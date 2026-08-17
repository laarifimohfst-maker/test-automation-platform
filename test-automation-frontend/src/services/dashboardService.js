import api from './api';

export const obtenirDashboard = async (utilisateurId) => {
  const response = await api.get('/dashboard', {
    params: { utilisateurId },
  });
  return response.data;
};