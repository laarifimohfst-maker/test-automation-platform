import api from '../commun/api';

export const obtenirRapportsAdministration = async (filtres = {}) => {
  const params = Object.fromEntries(
    Object.entries(filtres).filter(([, valeur]) =>
      valeur !== undefined && valeur !== null && valeur !== '' && valeur !== 'TOUS'
    )
  );

  const reponse = await api.get('/rapports', { params });
  return reponse.data;
};

export const obtenirRapportAdministration = async (id) => {
  const reponse = await api.get(`/rapports/${id}`);
  return reponse.data;
};

export const telechargerRapportAdministration = async (id) => {
  const reponse = await api.get(`/rapports/${id}/download`, {
    responseType: 'blob',
  });
  return reponse.data;
};

export const enregistrerRapportTelecharge = (fichier, nomRapport) => {
  const url = URL.createObjectURL(fichier);
  const lien = document.createElement('a');
  const nomSecurise = (nomRapport || 'rapport')
    .replace(/[<>:"/\\|?*]/g, '_')
    .trim();

  lien.href = url;
  lien.download = `${nomSecurise || 'rapport'}.pdf`;
  document.body.appendChild(lien);
  lien.click();
  lien.remove();
  URL.revokeObjectURL(url);
};

export const supprimerRapportAdministration = async (id) => {
  await api.delete(`/rapports/${id}`);
};
