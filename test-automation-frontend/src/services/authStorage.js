const CLE_JETON = 'testAutomation.accessToken';
const CLE_UTILISATEUR = 'testAutomation.utilisateur';

export const obtenirJeton = () => sessionStorage.getItem(CLE_JETON);

export const obtenirUtilisateurEnregistre = () => {
  const valeur = sessionStorage.getItem(CLE_UTILISATEUR);

  if (!valeur) return null;

  try {
    return JSON.parse(valeur);
  } catch {
    sessionStorage.removeItem(CLE_UTILISATEUR);
    return null;
  }
};

export const obtenirUtilisateurId = () => obtenirUtilisateurEnregistre()?.id;

export const enregistrerJeton = (jeton) => {
  sessionStorage.setItem(CLE_JETON, jeton);
};

export const enregistrerUtilisateur = (utilisateur) => {
  sessionStorage.setItem(CLE_UTILISATEUR, JSON.stringify(utilisateur));
};

export const effacerSession = () => {
  sessionStorage.removeItem(CLE_JETON);
  sessionStorage.removeItem(CLE_UTILISATEUR);
};
