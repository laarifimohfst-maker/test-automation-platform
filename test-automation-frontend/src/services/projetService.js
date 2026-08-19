import api from './api';

export const obtenirProjetsUtilisateur = (utilisateurId) =>
  api.get(`/projets/utilisateur/${utilisateurId}`);

export const importerProjetZip = (fichier, utilisateurId) => {
  const formData = new FormData();
  formData.append('fichier', fichier);
  return api.post(`/projets/import?utilisateurId=${utilisateurId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const importerProjetGithub = (url, utilisateurId) =>
  api.post(`/projets/import/github?url=${encodeURIComponent(url)}&utilisateurId=${utilisateurId}`);

export const supprimerProjet = (id) =>
  api.delete(`/projets/${id}`);

export const modifierProjet = (id, projet) =>
  api.put(`/projets/${id}`, projet);