import api from '../commun/api';

export const obtenirDashboard = async (utilisateurId) => {
  const response = await api.get('/dashboard', {
    params: { utilisateurId },
  });
  return response.data;
};
