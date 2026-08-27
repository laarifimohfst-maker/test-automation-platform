import { useCallback, useEffect, useState } from 'react';
import {
  Activity,
  FileText,
  FlaskConical,
  FolderKanban,
  LoaderCircle,
  PlayCircle,
  RefreshCw,
  TriangleAlert,
  Users,
} from 'lucide-react';
import { obtenirDashboardAdministrateur } from '../../services/adminDashboardService';
import './AdminAccueil.css';

const formaterDate = (date) => {
  if (!date) return '—';

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(date));
};

const libelleStatut = (statut) => statut?.replaceAll('_', ' ') || '—';

function CarteStatistique({ titre, valeur, details, icone, couleur }) {
  return (
    <article className="admin-stat-card" style={{ '--admin-card-color': couleur }}>
      <span className="admin-stat-icon">{icone}</span>
      <div className="admin-stat-content">
        <p>{titre}</p>
        <strong>{valeur ?? 0}</strong>
        <small>{details}</small>
      </div>
    </article>
  );
}

function MessageVide({ children }) {
  return (
    <div className="admin-empty-state">
      <Activity size={20} />
      <span>{children}</span>
    </div>
  );
}

function AdminAccueil() {
  const [dashboard, setDashboard] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');

  const chargerDashboard = useCallback(async () => {
    setChargement(true);
    setErreur('');

    try {
      setDashboard(await obtenirDashboardAdministrateur());
    } catch (erreurRequete) {
      setErreur(
        erreurRequete.response?.data?.message
          || 'Impossible de charger le tableau de bord administrateur.'
      );
    } finally {
      setChargement(false);
    }
  }, []);

  useEffect(() => {
    let composantActif = true;

    obtenirDashboardAdministrateur()
      .then((donnees) => {
        if (composantActif) setDashboard(donnees);
      })
      .catch((erreurRequete) => {
        if (composantActif) {
          setErreur(
            erreurRequete.response?.data?.message
              || 'Impossible de charger le tableau de bord administrateur.'
          );
        }
      })
      .finally(() => {
        if (composantActif) setChargement(false);
      });

    return () => {
      composantActif = false;
    };
  }, []);

  const statistiques = dashboard ? [
    {
      titre: 'Utilisateurs',
      valeur: dashboard.utilisateurs.total,
      details: `${dashboard.utilisateurs.actifs} actifs · ${dashboard.utilisateurs.inactifs} inactifs`,
      icone: <Users size={21} />,
      couleur: '#2563eb',
    },
    {
      titre: 'Projets',
      valeur: dashboard.projets.total,
      details: `${dashboard.projets.importes} importés · ${dashboard.projets.enErreur} en erreur`,
      icone: <FolderKanban size={21} />,
      couleur: '#7c3aed',
    },
    {
      titre: 'Exécutions',
      valeur: dashboard.executions.total,
      details: `${dashboard.executions.terminees} terminées · ${dashboard.executions.echouees} échouées`,
      icone: <PlayCircle size={21} />,
      couleur: '#0891b2',
    },
    {
      titre: 'Tests',
      valeur: dashboard.tests.total,
      details: `${dashboard.tests.reussis} réussis · ${dashboard.tests.echoues} échoués`,
      icone: <FlaskConical size={21} />,
      couleur: '#16a34a',
    },
    {
      titre: 'Rapports',
      valeur: dashboard.rapports.total,
      details: `${dashboard.rapports.tests} tests · ${dashboard.rapports.analysesQualite} qualité`,
      icone: <FileText size={21} />,
      couleur: '#ea580c',
    },
  ] : [];

  return (
    <section className="admin-dashboard-content">
        <div className="admin-dashboard-toolbar">
          <button type="button" className="admin-refresh-button" onClick={chargerDashboard} disabled={chargement}>
            <RefreshCw size={17} className={chargement ? 'admin-spin' : ''} />
            Actualiser
          </button>
        </div>

        {chargement && !dashboard && (
          <div className="admin-feedback">
            <LoaderCircle className="admin-spin" size={28} />
            <span>Chargement du tableau de bord...</span>
          </div>
        )}

        {erreur && !dashboard && (
          <div className="admin-feedback admin-feedback-error">
            <TriangleAlert size={28} />
            <strong>Le chargement a échoué</strong>
            <span>{erreur}</span>
            <button type="button" onClick={chargerDashboard}>Réessayer</button>
          </div>
        )}

        {dashboard && (
          <>
            {erreur && (
              <div className="admin-inline-error">
                <TriangleAlert size={18} /> {erreur}
              </div>
            )}

            <section className="admin-stats-grid" aria-label="Statistiques globales">
              {statistiques.map((statistique) => (
                <CarteStatistique key={statistique.titre} {...statistique} />
              ))}
            </section>

            <section className="admin-activity-section">
              <div className="admin-section-heading">
                <div>
                  <span>Activité récente</span>
                  <h2>Dernières opérations</h2>
                </div>
              </div>

              <div className="admin-activity-grid">
                <article className="admin-activity-card">
                  <h3><FolderKanban size={19} /> Derniers projets</h3>
                  {dashboard.activiteRecente.derniersProjets.length === 0 ? (
                    <MessageVide>Aucun projet récent.</MessageVide>
                  ) : (
                    <div className="admin-activity-list">
                      {dashboard.activiteRecente.derniersProjets.map((projet) => (
                        <div className="admin-activity-item" key={projet.id}>
                          <div>
                            <strong>{projet.nom}</strong>
                            <span>{projet.utilisateur.nom} · {projet.typeSource}</span>
                          </div>
                          <div className="admin-activity-meta">
                            <span className={`admin-status admin-status-${projet.statut?.toLowerCase()}`}>
                              {libelleStatut(projet.statut)}
                            </span>
                            <time>{formaterDate(projet.dateImport)}</time>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </article>

                <article className="admin-activity-card">
                  <h3><PlayCircle size={19} /> Dernières exécutions</h3>
                  {dashboard.activiteRecente.dernieresExecutions.length === 0 ? (
                    <MessageVide>Aucune exécution récente.</MessageVide>
                  ) : (
                    <div className="admin-activity-list">
                      {dashboard.activiteRecente.dernieresExecutions.map((execution) => (
                        <div className="admin-activity-item" key={execution.id}>
                          <div>
                            <strong>{execution.projet.nom}</strong>
                            <span>{execution.utilisateur.nom} · {libelleStatut(execution.type)}</span>
                          </div>
                          <div className="admin-activity-meta">
                            <span className={`admin-status admin-status-${execution.statut?.toLowerCase()}`}>
                              {libelleStatut(execution.statut)}
                            </span>
                            <time>{formaterDate(execution.dateDebut)}</time>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </article>

                <article className="admin-activity-card">
                  <h3><FileText size={19} /> Derniers rapports</h3>
                  {dashboard.activiteRecente.derniersRapports.length === 0 ? (
                    <MessageVide>Aucun rapport récent.</MessageVide>
                  ) : (
                    <div className="admin-activity-list">
                      {dashboard.activiteRecente.derniersRapports.map((rapport) => (
                        <div className="admin-activity-item" key={rapport.id}>
                          <div>
                            <strong>{rapport.nom}</strong>
                            <span>{rapport.projet.nom} · {rapport.utilisateur.nom}</span>
                          </div>
                          <div className="admin-activity-meta">
                            <span className="admin-report-type">{libelleStatut(rapport.type)}</span>
                            <time>{formaterDate(rapport.dateGeneration)}</time>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </article>
              </div>
            </section>
          </>
        )}
    </section>
  );
}

export default AdminAccueil;
