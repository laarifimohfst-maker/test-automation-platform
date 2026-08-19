import { useLocation } from 'react-router-dom';
import { useState, useRef } from 'react';
import { Upload, GitBranch, X } from 'lucide-react';
import { importerProjetZip, importerProjetGithub } from '../services/projetService';

const UTILISATEUR_ID = 1; // en dur pour l'instant, comme le reste de l'app

function Header() {
  const location = useLocation();
  const estProjets = location.pathname === '/projets';
  const estConfiguration = location.pathname === '/configurations';

  const [menuImport, setMenuImport] = useState(false);
  const [modaleGithub, setModaleGithub] = useState(false);
  const [urlGithub, setUrlGithub] = useState('');
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);

  // input file caché, déclenché par le bouton "Importer un fichier ZIP"
  const inputFileRef = useRef(null);

  // Prévient Projets.jsx qu'un import a réussi, pour qu'il recharge la liste
  const prevenirImportReussi = () => {
    window.dispatchEvent(new CustomEvent('projetImporte'));
  };

  const gererChoixFichier = (e) => {
    const fichier = e.target.files[0];
    if (!fichier) return;

    setEnCours(true);
    setErreur(null);

    importerProjetZip(fichier, UTILISATEUR_ID)
      .then(() => {
        setMenuImport(false);
        prevenirImportReussi();
      })
      .catch((err) => {
        console.error('Erreur import ZIP', err);
        // Le backend renvoie un message précis (ex: "pas un projet Spring Boot valide")
        // en badRequest(), on l'affiche tel quel s'il existe.
        const message = err.response?.data || "Échec de l'import du fichier ZIP.";
        setErreur(message);
      })
      .finally(() => {
        setEnCours(false);
        e.target.value = ''; // permet de réimporter le même fichier plus tard
      });
  };

  const gererImportGithub = () => {
    if (!urlGithub.trim()) return;

    setEnCours(true);
    setErreur(null);

    importerProjetGithub(urlGithub.trim(), UTILISATEUR_ID)
      .then(() => {
        setModaleGithub(false);
        setMenuImport(false);
        setUrlGithub('');
        prevenirImportReussi();
      })
      .catch((err) => {
        console.error('Erreur import GitHub', err);
        const message = err.response?.data || 'Échec du clonage du dépôt GitHub.';
        setErreur(message);
      })
      .finally(() => setEnCours(false));
  };

  return (
    <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px' }}>
      <div>
        {estProjets ? (
          <>
            <h2 style={{ margin: 0, color: '#111827', fontSize: '22px' }}>Mes projets</h2>
            <p style={{ margin: '4px 0 0', color: '#6b7280' }}>Gérez et consultez tous vos projets</p>
          </>
        ) : estConfiguration ? (
          <>
            <h2 style={{ margin: 0, color: '#111827', fontSize: '22px' }}>Configuration des tests</h2>
            <p style={{ margin: '4px 0 0', color: '#6b7280' }}>Choisissez les types de tests à exécuter</p>
          </>
        ) : (
          <>
            <h2 style={{ margin: 0, color: '#111827' }}>Bonjour 👋</h2>
            <p style={{ margin: '4px 0 0', color: '#6b7280' }}>Voici un aperçu de vos projets et exécutions.</p>
          </>
        )}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        {estProjets && (
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setMenuImport(!menuImport)}
              style={{
                display: 'flex', alignItems: 'center', gap: '7px',
                padding: '11px 18px', border: 'none', borderRadius: '7px',
                backgroundColor: '#7c3aed', color: '#fff',
                cursor: 'pointer', fontWeight: '600', fontSize: '14px'
              }}
            >
              + Nouveau projet
            </button>

            {menuImport && (
              <div style={{
                position: 'absolute', top: '48px', right: 0, width: '250px',
                backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.1)', zIndex: 100
              }}>
                <div style={{ padding: '12px 15px', borderBottom: '1px solid #f3f4f6', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: '600', color: '#111827', fontSize: '13px' }}>Importer un projet</span>
                  <X size={16} color="#9ca3af" style={{ cursor: 'pointer' }} onClick={() => setMenuImport(false)} />
                </div>

                <button
                  onClick={() => inputFileRef.current.click()}
                  disabled={enCours}
                  style={{ width: '100%', display: 'flex', alignItems: 'center', gap: '12px', padding: '13px 15px', border: 'none', backgroundColor: '#fff', cursor: enCours ? 'not-allowed' : 'pointer', textAlign: 'left' }}
                >
                  <Upload size={19} color="#7c3aed" />
                  <div>
                    <div style={{ fontWeight: '600', color: '#111827', fontSize: '13px' }}>Importer un fichier ZIP</div>
                    <div style={{ color: '#9ca3af', fontSize: '11px', marginTop: '3px' }}>Depuis votre ordinateur</div>
                  </div>
                </button>
                <input
                  ref={inputFileRef}
                  type="file"
                  accept=".zip"
                  onChange={gererChoixFichier}
                  style={{ display: 'none' }}
                />

                <button
                  onClick={() => { setModaleGithub(true); setMenuImport(false); }}
                  style={{ width: '100%', display: 'flex', alignItems: 'center', gap: '12px', padding: '13px 15px', border: 'none', borderTop: '1px solid #f3f4f6', backgroundColor: '#fff', cursor: 'pointer', textAlign: 'left' }}
                >
                  <GitBranch size={19} color="#374151" />
                  <div>
                    <div style={{ fontWeight: '600', color: '#111827', fontSize: '13px' }}>Cloner depuis GitHub</div>
                    <div style={{ color: '#9ca3af', fontSize: '11px', marginTop: '3px' }}>Depuis un dépôt GitHub</div>
                  </div>
                </button>

                {erreur && (
                  <div style={{ padding: '10px 15px', color: '#dc2626', fontSize: '12px' }}>{erreur}</div>
                )}
              </div>
            )}
          </div>
        )}

        <span style={{ position: 'relative' }}>
          🔔
          <span style={{
            position: 'absolute', top: '-6px', right: '-8px',
            width: '16px', height: '16px', borderRadius: '50%',
            backgroundColor: '#ef4444', color: '#fff', fontSize: '10px',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold'
          }}>3</span>
        </span>

        <div style={{
          width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#c4b5fd',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: '#fff', fontWeight: '600', fontSize: '13px'
        }}>
          YH
        </div>
      </div>

      {/* --- Modale GitHub --- */}
      {modaleGithub && (
        <div style={{
          position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.4)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200
        }}>
          <div style={{ backgroundColor: '#fff', borderRadius: '10px', padding: '24px', width: '400px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ margin: 0, fontSize: '16px' }}>Cloner depuis GitHub</h3>
              <X size={18} style={{ cursor: 'pointer' }} onClick={() => setModaleGithub(false)} />
            </div>

            <input
              type="text"
              placeholder="https://github.com/utilisateur/depot.git"
              value={urlGithub}
              onChange={(e) => setUrlGithub(e.target.value)}
              style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #e5e7eb', fontSize: '14px', boxSizing: 'border-box', marginBottom: '12px' }}
            />

            {erreur && <div style={{ color: '#dc2626', fontSize: '13px', marginBottom: '12px' }}>{erreur}</div>}

            <button
              onClick={gererImportGithub}
              disabled={enCours || !urlGithub.trim()}
              style={{
                width: '100%', padding: '10px', border: 'none', borderRadius: '6px',
                backgroundColor: '#7c3aed', color: '#fff', fontWeight: '600',
                cursor: enCours ? 'not-allowed' : 'pointer'
              }}
            >
              {enCours ? 'Clonage en cours...' : 'Importer'}
            </button>
          </div>
        </div>
      )}
    </header>
  );
}

export default Header;