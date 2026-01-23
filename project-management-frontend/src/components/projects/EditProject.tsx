import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectsApi } from '../../api/projects';
import { useToast } from '../../hooks/useToast';
import Card from '../common/Card';
import Button from '../common/Button';
import ProjectForm from './ProjectForm';
import LoadingSpinner from '../common/LoadingSpinner';
import { ChevronLeft } from 'lucide-react';

const EditProject: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { success, error } = useToast();
  const [project, setProject] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!id) {
      error('Invalid project ID');
      navigate('/projects');
      return;
    }
    
    const projectId = parseInt(id);
    if (isNaN(projectId)) {
      error('Invalid project ID format');
      navigate('/projects');
      return;
    }
    
    fetchProject(projectId);
  }, [id]);

  const fetchProject = async (projectId: number) => {
    try {
      setIsLoading(true);
      const response = await projectsApi.getById(projectId);
      setProject(response.data);
    } catch (err: any) {
      console.error('Error loading project:', err);
      if (err.response?.status === 404) {
        error('Project not found');
        navigate('/projects');
      } else {
        error('Failed to load project');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSuccess = () => {
    success('Project updated successfully');
    navigate(`/projects/${id}`);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!project) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4">
        <Card className="max-w-md">
          <div className="p-6 text-center py-12">
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
              Project not found
            </h3>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              The project you're looking for doesn't exist.
            </p>
            <Button onClick={() => navigate('/projects')}>
              <ChevronLeft className="h-4 w-4 mr-2" />
              Back to Projects
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-6">
      <div className="mb-6">
        <Button
          variant="ghost"
          onClick={() => navigate(`/projects/${id}`)}
          className="flex items-center mb-4"
        >
          <ChevronLeft className="h-4 w-4 mr-2" />
          Back to Project
        </Button>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Edit Project
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-2">
          Update project details and settings
        </p>
      </div>

      <Card>
        <div className="p-6">
          <ProjectForm project={project} onSuccess={handleSuccess} />
        </div>
      </Card>
    </div>
  );
};

export default EditProject;