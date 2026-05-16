import api from '.';

export const oauthApi = {
  getAuthorizationUrl: (provider: string) => 
    api.get<{ url: string }>(`/oauth2/url/${provider}`),
};