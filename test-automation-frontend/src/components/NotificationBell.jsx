import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  AlertCircle,
  AlertTriangle,
  Bell,
  CheckCheck,
  CheckCircle2,
  Info,
  Trash2,
} from 'lucide-react';

import {
  marquerNotificationCommeLue,
  obtenirNotificationsUtilisateur,
  supprimerNotification,
} from '../services/notificationService';

import './NotificationBell.css';
import { obtenirUtilisateurId } from '../services/authStorage';

const INTERVALLE_ACTUALISATION = 30000;

function NotificationBell() {
  const [ouvert, setOuvert] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [actionId, setActionId] = useState(null);
  const [lectureGlobale, setLectureGlobale] = useState(false);
  const conteneurRef = useRef(null);

  const chargerNotifications = useCallback(async (avecChargement = false) => {
    try {
      const response =
        await obtenirNotificationsUtilisateur(obtenirUtilisateurId());

      const notificationsTriees = [...response.data].sort(
        (a, b) => new Date(b.dateEnvoi || 0) - new Date(a.dateEnvoi || 0)
      );

      setNotifications(notificationsTriees);
      setErreur(null);
    } catch (err) {
      console.error('Erreur chargement notifications :', err);
      setErreur('Impossible de charger les notifications.');
    } finally {
      if (avecChargement) setChargement(false);
    }
  }, []);

  useEffect(() => {
    const demarrage = window.setTimeout(
      () => chargerNotifications(true),
      0
    );

    const intervalle = window.setInterval(
      () => chargerNotifications(false),
      INTERVALLE_ACTUALISATION
    );

    return () => {
      window.clearTimeout(demarrage);
      window.clearInterval(intervalle);
    };
  }, [chargerNotifications]);

  useEffect(() => {
    const fermerAuClicExterieur = (event) => {
      if (
        conteneurRef.current &&
        !conteneurRef.current.contains(event.target)
      ) {
        setOuvert(false);
      }
    };

    document.addEventListener('mousedown', fermerAuClicExterieur);
    return () =>
      document.removeEventListener('mousedown', fermerAuClicExterieur);
  }, []);

  const notificationsNonLues = useMemo(
    () => notifications.filter((notification) => !notification.lue),
    [notifications]
  );

  const basculerMenu = () => {
    const prochainEtat = !ouvert;
    setOuvert(prochainEtat);

    if (prochainEtat) {
      chargerNotifications(false);
    }
  };

  const marquerCommeLue = async (notification) => {
    if (notification.lue || actionId !== null) return;

    setActionId(notification.id);
    setErreur(null);

    try {
      const response =
        await marquerNotificationCommeLue(notification.id);

      setNotifications((liste) =>
        liste.map((element) =>
          element.id === notification.id
            ? { ...element, ...response.data, lue: true }
            : element
        )
      );
    } catch (err) {
      console.error('Erreur lecture notification :', err);
      setErreur('Impossible de marquer la notification comme lue.');
    } finally {
      setActionId(null);
    }
  };

  const toutMarquerCommeLu = async () => {
    if (notificationsNonLues.length === 0 || lectureGlobale) return;

    setLectureGlobale(true);
    setErreur(null);

    try {
      await Promise.all(
        notificationsNonLues.map((notification) =>
          marquerNotificationCommeLue(notification.id)
        )
      );

      setNotifications((liste) =>
        liste.map((notification) => ({ ...notification, lue: true }))
      );
    } catch (err) {
      console.error('Erreur lecture des notifications :', err);
      setErreur('Certaines notifications n’ont pas pu être marquées comme lues.');
      await chargerNotifications(false);
    } finally {
      setLectureGlobale(false);
    }
  };

  const gererSuppression = async (event, notificationId) => {
    event.stopPropagation();
    if (actionId !== null) return;

    setActionId(notificationId);
    setErreur(null);

    try {
      await supprimerNotification(notificationId);
      setNotifications((liste) =>
        liste.filter((notification) => notification.id !== notificationId)
      );
    } catch (err) {
      console.error('Erreur suppression notification :', err);
      setErreur('Impossible de supprimer la notification.');
    } finally {
      setActionId(null);
    }
  };

  const formaterDate = (date) => {
    if (!date) return '';

    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(date));
  };

  const iconeType = (type) => {
    switch (type) {
      case 'SUCCES':
        return <CheckCircle2 size={18} />;
      case 'ECHEC':
        return <AlertCircle size={18} />;
      case 'AVERTISSEMENT':
        return <AlertTriangle size={18} />;
      default:
        return <Info size={18} />;
    }
  };

  return (
    <div className="notification-container" ref={conteneurRef}>
      <button
        type="button"
        className="notification-bell-button"
        aria-label="Afficher les notifications"
        aria-expanded={ouvert}
        onClick={basculerMenu}
      >
        <Bell size={21} />

        {notificationsNonLues.length > 0 && (
          <span className="notification-count">
            {notificationsNonLues.length > 99
              ? '99+'
              : notificationsNonLues.length}
          </span>
        )}
      </button>

      {ouvert && (
        <div className="notification-menu">
          <div className="notification-header">
            <div>
              <strong>Notifications</strong>
              <span>
                {notificationsNonLues.length}{' '}
                {notificationsNonLues.length > 1 ? 'non lues' : 'non lue'}
              </span>
            </div>

            {notificationsNonLues.length > 0 && (
              <button
                type="button"
                className="notification-read-all"
                disabled={lectureGlobale}
                onClick={toutMarquerCommeLu}
              >
                <CheckCheck size={15} />
                Tout lire
              </button>
            )}
          </div>

          {erreur && <div className="notification-error">{erreur}</div>}

          <div className="notification-list">
            {chargement ? (
              <div className="notification-empty">Chargement...</div>
            ) : notifications.length === 0 ? (
              <div className="notification-empty">
                <Bell size={26} />
                Aucune notification
              </div>
            ) : (
              notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`notification-item ${
                    notification.lue ? '' : 'notification-item-unread'
                  }`}
                  role="button"
                  tabIndex={0}
                  onClick={() => marquerCommeLue(notification)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                      marquerCommeLue(notification);
                    }
                  }}
                >
                  <span
                    className={`notification-type notification-type-${
                      notification.type?.toLowerCase() || 'information'
                    }`}
                  >
                    {iconeType(notification.type)}
                  </span>

                  <div className="notification-content">
                    <p>{notification.message}</p>
                    <div className="notification-meta">
                      <span>{formaterDate(notification.dateEnvoi)}</span>
                      {notification.execution?.id && (
                        <span>Exécution #{notification.execution.id}</span>
                      )}
                    </div>
                  </div>

                  <div className="notification-item-actions">
                    {!notification.lue && (
                      <span
                        className="notification-unread-dot"
                        title="Non lue"
                      />
                    )}

                    <button
                      type="button"
                      className="notification-delete"
                      title="Supprimer"
                      aria-label={`Supprimer la notification ${notification.id}`}
                      disabled={actionId === notification.id}
                      onClick={(event) =>
                        gererSuppression(event, notification.id)
                      }
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default NotificationBell;
