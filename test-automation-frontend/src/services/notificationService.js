import api from './api';

export const obtenirNotificationsUtilisateur = (utilisateurId) =>
  api.get(`/notifications/utilisateur/${utilisateurId}`);

export const marquerNotificationCommeLue = (notificationId) =>
  api.put(`/notifications/${notificationId}/lire`);

export const supprimerNotification = (notificationId) =>
  api.delete(`/notifications/${notificationId}`);
