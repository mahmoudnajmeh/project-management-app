import React, { useState, useRef, useEffect } from 'react';
import { Bot, Send, X, Loader2 } from 'lucide-react';
import api from '../../api/api';
import Button from '../common/Button';
import Modal from '../common/Modal';
import { useToast } from '../../hooks/useToast';

interface ConversationEntry {
  id: number;
  question: string;
  answer: string;
  timestamp: Date;
}

const AIAssistant: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [question, setQuestion] = useState('');
  const [conversations, setConversations] = useState<ConversationEntry[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { error, success } = useToast();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load saved conversations from localStorage on mount
  useEffect(() => {
    const saved = localStorage.getItem('ai_conversations');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        // Convert timestamp strings back to Date objects
        const withDates = parsed.map((conv: any) => ({
          ...conv,
          timestamp: new Date(conv.timestamp)
        }));
        setConversations(withDates);
      } catch (e) {
        console.error('Failed to load saved conversations:', e);
      }
    }
  }, []);

  // Save conversations to localStorage whenever they change
  useEffect(() => {
    localStorage.setItem('ai_conversations', JSON.stringify(conversations));
  }, [conversations]);

  // Auto-scroll to bottom when new message arrives
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [conversations]);

  const handleAsk = async () => {
    if (!question.trim()) {
      error('Please enter a question');
      return;
    }

    setIsLoading(true);
    const currentQuestion = question;
    setQuestion('');

    try {
      const response = await api.post('/ai/ask', { question: currentQuestion });
      const newConversation: ConversationEntry = {
        id: Date.now(),
        question: currentQuestion,
        answer: response.data.answer,
        timestamp: new Date()
      };
      setConversations(prev => [...prev, newConversation]);
      success('Got response from AI assistant!');
    } catch (err) {
      console.error('AI error:', err);
      error('Failed to get AI response');
      setQuestion(currentQuestion);
    } finally {
      setIsLoading(false);
    }
  };

  const clearHistory = () => {
    setConversations([]);
    localStorage.removeItem('ai_conversations');
    success('Conversation history cleared');
  };

  const formatTime = (timestamp: Date) => {
    return timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <>
      {/* Floating button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 z-[100] p-4 bg-primary-600 text-white rounded-full shadow-lg hover:bg-primary-700 transition-all duration-200"
      >
        <Bot className="h-6 w-6" />
      </button>

      <Modal
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        title="AI Assistant"
        size="lg"
      >
        <div className="space-y-4 h-[600px] flex flex-col">
          <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg flex-shrink-0">
            <p className="text-sm text-blue-700 dark:text-blue-300">
              🤖 Ask me anything about your tasks, projects, or team. I can help you find information, suggest priorities, and more!
            </p>
          </div>

          {/* Conversation History */}
          <div className="flex-1 overflow-y-auto space-y-4 min-h-0">
            {conversations.length === 0 ? (
              <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                <Bot className="h-12 w-12 mx-auto mb-2 opacity-50" />
                <p>No conversations yet. Ask me something!</p>
              </div>
            ) : (
              conversations.map((conv) => (
                <div key={conv.id} className="space-y-2">
                  <div className="bg-gray-100 dark:bg-gray-800 rounded-lg p-3">
                    <div className="flex items-start space-x-2">
                      <div className="flex-shrink-0">
                        <div className="h-6 w-6 rounded-full bg-primary-500 flex items-center justify-center">
                          <span className="text-xs text-white">You</span>
                        </div>
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center justify-between">
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            {formatTime(conv.timestamp)}
                          </p>
                        </div>
                        <p className="text-sm text-gray-900 dark:text-white mt-1">
                          {conv.question}
                        </p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-3 ml-4">
                    <div className="flex items-start space-x-2">
                      <div className="flex-shrink-0">
                        <Bot className="h-5 w-5 text-primary-600" />
                      </div>
                      <div className="flex-1">
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          AI Assistant
                        </p>
                        <p className="text-sm text-gray-700 dark:text-gray-300 mt-1 whitespace-pre-wrap">
                          {conv.answer}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Loading indicator */}
          {isLoading && (
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-3 ml-4">
              <div className="flex items-start space-x-2">
                <Bot className="h-5 w-5 text-primary-600 animate-pulse" />
                <div className="flex-1">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Thinking...</p>
                </div>
              </div>
            </div>
          )}

          {/* Input area */}
          <div className="space-y-2 flex-shrink-0 border-t border-gray-200 dark:border-gray-700 pt-4">
            <div className="flex space-x-2">
              <textarea
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                placeholder="Ask a question..."
                rows={2}
                className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent dark:bg-gray-800 dark:text-white resize-none"
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleAsk();
                  }
                }}
              />
              <Button
                onClick={handleAsk}
                disabled={isLoading || !question.trim()}
                className="self-end"
              >
                {isLoading ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Send className="h-4 w-4" />
                )}
              </Button>
            </div>
            {conversations.length > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={clearHistory}
                className="text-xs text-gray-500 hover:text-red-500"
              >
                Clear History
              </Button>
            )}
          </div>
        </div>
      </Modal>
    </>
  );
};

export default AIAssistant;