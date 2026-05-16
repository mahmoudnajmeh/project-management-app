import React, { useState, useEffect, useRef, useCallback } from 'react';
import { 
  X, Send, Paperclip, Smile, Check, CheckCheck, Download, 
  FileText, FileSpreadsheet, FileArchive, FileCode, 
  FileVideo, FileAudio, File, Image as ImageIcon, XCircle, Maximize2 
} from 'lucide-react';
import { useChat } from '../../hooks/useChat';
import { useAuth } from '../../hooks/useAuth';
import Button from '../common/Button';
import Input from '../common/Input';
import { chatApi } from '../../api/chat';
import EmojiPicker from 'emoji-picker-react';
import { useDropzone } from 'react-dropzone';
import { useToast } from '../../hooks/useToast';

interface ChatModalProps {
  receiverId: number;
  receiverName: string;
  receiverAvatar?: string | null;
  isOpen: boolean;
  onClose: () => void;
}

interface FileData {
  storedName: string;
  originalName: string;
}

interface FileAttachment {
  storedName: string;
  originalName: string;
  fileUrl: string;
  isImage: boolean;
  isPdf: boolean;
  extension: string;
}

const ChatModal: React.FC<ChatModalProps> = ({
  receiverId,
  receiverName,
  receiverAvatar,
  isOpen,
  onClose
}) => {
  const { user } = useAuth();
  const { error: showError } = useToast();
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [attachments, setAttachments] = useState<File[]>([]);
  const [uploadingFiles, setUploadingFiles] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [selectedImageName, setSelectedImageName] = useState<string>('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutRef = useRef<number | null>(null);
  const lastTypingSentRef = useRef<number>(0);

  const {
    messages,
    sendMessage: sendChatMessage,
    sendTyping,
    markAsRead,
    isConnected,
    typingUsers
  } = useChat({
    receiverId,
    onMessageReceived: () => {
      if (user) {
        markAsRead();
      }
    }
  });

  const onDrop = useCallback((acceptedFiles: File[]) => {
    setAttachments(prev => [...prev, ...acceptedFiles]);
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: true,
    noClick: true
  });

  useEffect(() => {
    if (isOpen && receiverId && user) {
      setLoading(true);
      chatApi.getConversation(user.id, receiverId)
        .then(() => {
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
        });
    }
  }, [isOpen, receiverId, user]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const getFileIcon = (extension: string, isImage: boolean) => {
    if (isImage) return <ImageIcon className="h-8 w-8 text-blue-500" />;
    
    const officeExtensions = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'];
    const pdfExtensions = ['pdf'];
    const spreadsheetExtensions = ['xls', 'xlsx', 'csv', 'ods'];
    const archiveExtensions = ['zip', 'rar', '7z', 'tar', 'gz', 'bz2'];
    const codeExtensions = ['js', 'ts', 'jsx', 'tsx', 'html', 'css', 'json', 'xml', 'java', 'py', 'cpp', 'c', 'h', 'php', 'class', 'jar'];
    const videoExtensions = ['mp4', 'avi', 'mkv', 'mov', 'wmv', 'flv', 'webm'];
    const audioExtensions = ['mp3', 'wav', 'ogg', 'flac', 'aac', 'm4a'];
    
    if (officeExtensions.includes(extension)) return <FileText className="h-8 w-8 text-blue-600" />;
    if (pdfExtensions.includes(extension)) return <FileText className="h-8 w-8 text-red-600" />;
    if (spreadsheetExtensions.includes(extension)) return <FileSpreadsheet className="h-8 w-8 text-green-600" />;
    if (archiveExtensions.includes(extension)) return <FileArchive className="h-8 w-8 text-yellow-600" />;
    if (codeExtensions.includes(extension)) return <FileCode className="h-8 w-8 text-purple-600" />;
    if (videoExtensions.includes(extension)) return <FileVideo className="h-8 w-8 text-red-600" />;
    if (audioExtensions.includes(extension)) return <FileAudio className="h-8 w-8 text-pink-600" />;
    
    return <File className="h-8 w-8 text-gray-600" />;
  };

  const parseFileAttachments = (content: string): FileAttachment[] => {
    const files: FileAttachment[] = [];
    const lines = content.split('\n');
    
    for (const line of lines) {
      if (line.startsWith('📎 ')) {
        try {
          const fileData: FileData = JSON.parse(line.substring(2));
          const extension = fileData.originalName.split('.').pop()?.toLowerCase() || '';
          const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'];
          
          const isImage = imageExtensions.includes(extension);
          const isPdf = extension === 'pdf';
          
          files.push({
            storedName: fileData.storedName,
            originalName: fileData.originalName,
            fileUrl: `http://localhost:8080/api/chat/file/${fileData.storedName}`,
            isImage: isImage,
            isPdf: isPdf,
            extension: extension
          });
        } catch (e) {
          console.error('Error parsing file:', e);
        }
      }
    }
    
    return files;
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if ((!message.trim() && attachments.length === 0) || !user) return;

    setUploadingFiles(true);
    
    try {
      let fullContent = message.trim();
      
      if (attachments.length > 0) {
        try {
          const formData = new FormData();
          attachments.forEach((file: File) => {
            formData.append('files', file);
          });
          
          const response = await chatApi.uploadFiles(formData) as any;
          const fileInfos = response.data.files;
          fullContent += '\n' + fileInfos.map((info: FileData) => `📎 ${JSON.stringify(info)}`).join('\n');
        } catch (uploadError) {
          showError('File upload failed');
          setUploadingFiles(false);
          return;
        }
      }

      sendChatMessage({
        type: 'CHAT',
        content: fullContent,
        senderId: user.id,
        senderName: user.username,
        receiverId: receiverId,
        read: false
      });

      setMessage('');
      setAttachments([]);
    } catch (error) {
      showError('Failed to send message');
    } finally {
      setUploadingFiles(false);
    }
  };

  const removeAttachment = (index: number) => {
    setAttachments(prev => prev.filter((_, i) => i !== index));
  };

  const onEmojiClick = (emojiData: any) => {
    setMessage(prev => prev + emojiData.emoji);
    setShowEmojiPicker(false);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setAttachments(prev => [...prev, ...Array.from(e.target.files!)]);
    }
  };

  const handleTyping = useCallback(() => {
    const now = Date.now();
    if (message.trim() && now - lastTypingSentRef.current > 2000) {
      sendTyping();
      lastTypingSentRef.current = now;
    }

    if (typingTimeoutRef.current) {
      window.clearTimeout(typingTimeoutRef.current);
    }
    
    if (message.trim()) {
      typingTimeoutRef.current = window.setTimeout(() => {
        typingTimeoutRef.current = null;
      }, 3000);
    }
  }, [message, sendTyping]);

  const formatTime = (timestamp: string) => {
    try {
      return new Date(timestamp).toLocaleTimeString([], { 
        hour: '2-digit', 
        minute: '2-digit' 
      });
    } catch {
      return 'Just now';
    }
  };

  const getInitials = (name: string) => {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  };

  const isReceiverTyping = typingUsers.includes(receiverId);

  const handleImageClick = (imageUrl: string, imageName: string) => {
    setSelectedImage(imageUrl);
    setSelectedImageName(imageName);
  };

  const handleFileClick = (file: FileAttachment) => {
    if (file.isImage) {
      handleImageClick(file.fileUrl, file.originalName);
    } else {
      window.open(file.fileUrl, '_blank');
    }
  };

  const handleDownloadClick = (file: FileAttachment) => {
    const link = document.createElement('a');
    link.href = file.fileUrl;
    link.download = file.originalName;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const renderMessageContent = (content: string) => {
    const files = parseFileAttachments(content);
    const textLines = content.split('\n').filter(line => !line.startsWith('📎 '));
    
    return (
      <>
        {textLines.map((line, i) => (
          line && <p key={i} className="text-sm break-words">{line}</p>
        ))}
        
        {files.map((file, i) => (
          <div key={i} className="mt-3">
            {file.isImage ? (
              <div className="relative group">
                <img 
                  src={file.fileUrl} 
                  alt={file.originalName}
                  className="max-w-full max-h-48 rounded-lg border border-gray-200 dark:border-gray-600 cursor-pointer hover:opacity-90 transition-opacity"
                  onClick={() => handleImageClick(file.fileUrl, file.originalName)}
                  onError={(e) => {
                    console.error('Failed to load:', file.originalName);
                    e.currentTarget.style.display = 'none';
                  }}
                />
                <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex space-x-1">
                  <button
                    onClick={() => handleImageClick(file.fileUrl, file.originalName)}
                    className="p-1.5 bg-black bg-opacity-60 text-white rounded-lg hover:bg-opacity-80"
                    title="View full size"
                  >
                    <Maximize2 className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => handleDownloadClick(file)}
                    className="p-1.5 bg-black bg-opacity-60 text-white rounded-lg hover:bg-opacity-80"
                    title="Download"
                  >
                    <Download className="h-4 w-4" />
                  </button>
                </div>
                <p className="mt-1 text-xs text-gray-500 truncate">{file.originalName}</p>
              </div>
            ) : (
              <div className="flex items-center space-x-3 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-600">
                <div className="flex-1 flex items-center space-x-3 min-w-0 cursor-pointer" onClick={() => handleFileClick(file)}>
                  {getFileIcon(file.extension, false)}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{file.originalName}</p>
                    <p className="text-xs text-gray-500">
                      {file.isPdf ? 'Click to view PDF' : 'Click to open'}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => handleDownloadClick(file)}
                  className="p-2 hover:bg-gray-200 dark:hover:bg-gray-500 rounded-full"
                  title="Download"
                >
                  <Download className="h-5 w-5 text-gray-600 dark:text-gray-300" />
                </button>
              </div>
            )}
          </div>
        ))}
      </>
    );
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="fixed inset-0 z-50 overflow-y-auto">
        <div className="flex min-h-full items-center justify-center p-4">
          <div className="fixed inset-0 bg-black bg-opacity-50" onClick={onClose}></div>
          
          <div className="relative bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl">
            <div className="flex items-center justify-between border-b border-gray-200 dark:border-gray-700 px-6 py-4">
              <div className="flex items-center space-x-3">
                <div className="relative">
                  {receiverAvatar ? (
                    <img
                      src={receiverAvatar}
                      alt={receiverName}
                      className="h-10 w-10 rounded-full object-cover border-2 border-white"
                    />
                  ) : (
                    <div className="h-10 w-10 rounded-full bg-primary-500 flex items-center justify-center">
                      <span className="text-sm font-bold text-white">
                        {getInitials(receiverName)}
                      </span>
                    </div>
                  )}
                  <div className={`absolute -bottom-1 -right-1 h-3 w-3 rounded-full border-2 border-white ${
                    isConnected ? 'bg-green-500' : 'bg-gray-400'
                  }`}></div>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900 dark:text-white">{receiverName}</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    {isConnected ? 'Online' : 'Connecting...'}
                    {isReceiverTyping && ' • Typing...'}
                  </p>
                </div>
              </div>
              <button onClick={onClose} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg">
                <X className="h-5 w-5 text-gray-500" />
              </button>
            </div>

            <div className="h-[500px] flex flex-col">
              <div {...getRootProps()} className={`flex-1 overflow-y-auto p-6 space-y-4 ${isDragActive ? 'bg-blue-50' : ''}`}>
                <input {...getInputProps()} />
                {isDragActive && (
                  <div className="absolute inset-0 flex items-center justify-center bg-blue-500 bg-opacity-10 border-2 border-blue-500 border-dashed rounded-lg">
                    <p className="text-lg font-semibold text-blue-600">Drop files to attach</p>
                  </div>
                )}
                
                {loading ? (
                  <div className="text-center py-12">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
                    <p className="text-gray-600 mt-2">Loading messages...</p>
                  </div>
                ) : messages.length === 0 ? (
                  <div className="text-center py-12">
                    <Send className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No messages yet</h3>
                    <p className="text-gray-600 dark:text-gray-400">Start a conversation with {receiverName}</p>
                  </div>
                ) : (
                  messages.map((msg, index) => (
                    <div key={msg.id || index} className={`flex ${msg.senderId === user?.id ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-xs lg:max-w-md ${msg.senderId === user?.id ? 'ml-12' : 'mr-12'}`}>
                        <div className={`rounded-lg px-4 py-2 ${
                          msg.senderId === user?.id
                            ? 'bg-primary-600 text-white rounded-br-none'
                            : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white rounded-bl-none'
                        }`}>
                          {renderMessageContent(msg.content)}
                        </div>
                        <div className={`flex items-center mt-1 text-xs ${
                          msg.senderId === user?.id ? 'justify-end' : 'justify-start'
                        }`}>
                          <span className="text-gray-500">{formatTime(msg.timestamp)}</span>
                          {msg.senderId === user?.id && (
                            <span className="ml-1">
                              {msg.read ? <CheckCheck className="h-3 w-3 text-blue-500" /> : <Check className="h-3 w-3 text-gray-400" />}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  ))
                )}
                {isReceiverTyping && (
                  <div className="flex justify-start">
                    <div className="bg-gray-100 dark:bg-gray-700 rounded-lg rounded-bl-none px-4 py-2">
                      <div className="flex space-x-1">
                        <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce"></div>
                        <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                        <div className="h-2 w-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
                      </div>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {attachments.length > 0 && (
                <div className="border-t border-gray-200 dark:border-gray-700 p-2 max-h-32 overflow-y-auto">
                  <div className="flex flex-wrap gap-2">
                    {attachments.map((file, index) => {
                      const extension = file.name.split('.').pop()?.toLowerCase() || '';
                      const isImage = ['jpg', 'jpeg', 'png', 'gif'].includes(extension);
                      
                      return (
                        <div key={index} className="relative group">
                          <div className="bg-gray-100 dark:bg-gray-700 rounded-lg p-2 pr-8 flex items-center space-x-2 max-w-[200px]">
                            {isImage ? <ImageIcon className="h-4 w-4" /> : <File className="h-4 w-4" />}
                            <span className="text-sm truncate">{file.name}</span>
                          </div>
                          <button
                            onClick={() => removeAttachment(index)}
                            className="absolute -top-1 -right-1 bg-red-500 text-white rounded-full p-0.5 opacity-0 group-hover:opacity-100"
                          >
                            <XCircle className="h-4 w-4" />
                          </button>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              <div className="border-t border-gray-200 dark:border-gray-700 p-4">
                <form onSubmit={handleSendMessage} className="flex items-center space-x-2">
                  <input type="file" id="fileInput" onChange={handleFileSelect} multiple className="hidden" />
                  <label htmlFor="fileInput" className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer">
                    <Paperclip className="h-5 w-5 text-gray-500" />
                  </label>
                  <div className="relative">
                    <button type="button" onClick={() => setShowEmojiPicker(!showEmojiPicker)} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg">
                      <Smile className="h-5 w-5 text-gray-500" />
                    </button>
                    {showEmojiPicker && (
                      <div className="absolute bottom-12 left-0 z-10">
                        <EmojiPicker onEmojiClick={onEmojiClick} />
                      </div>
                    )}
                  </div>
                  <div className="flex-1">
                    <Input
                      type="text"
                      value={message}
                      onChange={(e) => {
                        setMessage(e.target.value);
                        handleTyping();
                      }}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' && !e.shiftKey) {
                          e.preventDefault();
                          handleSendMessage(e);
                        }
                      }}
                      placeholder="Type your message..."
                      className="w-full"
                    />
                  </div>
                  <Button type="submit" disabled={(!message.trim() && attachments.length === 0) || !isConnected || uploadingFiles}>
                    {uploadingFiles ? <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div> : <Send className="h-4 w-4" />}
                  </Button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>

      {selectedImage && (
        <div 
          className="fixed inset-0 z-[60] flex items-center justify-center bg-black bg-opacity-90 p-4"
          onClick={() => {
            setSelectedImage(null);
            setSelectedImageName('');
          }}
        >
          <div className="relative max-w-4xl w-full bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
            <div className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white truncate max-w-md">
                {selectedImageName}
              </h3>
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => {
                    const link = document.createElement('a');
                    link.href = selectedImage;
                    link.download = selectedImageName;
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                  }}
                  className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
                  title="Download"
                >
                  <Download className="h-5 w-5 text-gray-600 dark:text-gray-300" />
                </button>
                <button
                  onClick={() => {
                    setSelectedImage(null);
                    setSelectedImageName('');
                  }}
                  className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
                >
                  <X className="h-5 w-5 text-gray-600 dark:text-gray-300" />
                </button>
              </div>
            </div>
            <div className="p-6 max-h-[70vh] overflow-auto flex items-center justify-center">
              <img 
                src={selectedImage} 
                alt={selectedImageName}
                className="max-w-full max-h-[60vh] object-contain"
              />
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ChatModal;