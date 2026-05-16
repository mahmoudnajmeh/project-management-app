import React, { useState, useEffect } from 'react';

interface TeamPhotoProps {
  teamId: number;
  alt: string;
  className?: string;
}

const TeamPhoto: React.FC<TeamPhotoProps> = ({ teamId, alt, className }) => {
  const [imageUrl, setImageUrl] = useState<string | null>(null);

  useEffect(() => {
    const fetchTeamPhoto = async () => {
      const token = localStorage.getItem('token');
      if (!token) return;

      try {
        const response = await fetch(`/api/teams/${teamId}/photo`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (response.ok) {
          const blob = await response.blob();
          const url = URL.createObjectURL(blob);
          setImageUrl(url);
        }
      } catch (error) {
        console.error('Error loading team photo:', error);
      }
    };

    fetchTeamPhoto();

    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [teamId]);

  if (!imageUrl) {
    return (
      <div className={`${className} bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center`}>
        <span className="text-white">Team</span>
      </div>
    );
  }

  return <img src={imageUrl} alt={alt} className={className} />;
};

export default TeamPhoto;