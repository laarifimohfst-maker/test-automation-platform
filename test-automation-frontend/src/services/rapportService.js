import api from './api';

export const genererRapportTests = (executionId) =>
  api.post('/rapports/tests', null, {
    params: { executionId },
  });

export const genererRapportAnalyseQualite = (executionId) =>
  api.post('/rapports/analyse-qualite', null, {
    params: { executionId },
  });

export const telechargerRapport = (rapportId) =>
  api.get(`/rapports/${rapportId}/download`, {
    responseType: 'blob',
  });

export const enregistrerFichierRapport = (fichier, nomRapport) => {
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
