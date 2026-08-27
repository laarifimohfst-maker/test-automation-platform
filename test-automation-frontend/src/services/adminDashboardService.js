import api from './api';

export const obtenirDashboardAdministrateur = async () => {
  const reponse = await api.get('/admin/dashboard');
  return reponse.data;
};
