import api from '../commun/api';

export const obtenirDashboardAdministrateur = async () => {
  const reponse = await api.get('/admin/dashboard');
  return reponse.data;
};
