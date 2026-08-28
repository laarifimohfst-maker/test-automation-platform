import { Navigate, Route, Routes } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './pages/developpeur/Dashboard';
import Projets from './pages/developpeur/Projets';
import ConfigurationTest from './pages/developpeur/ConfigurationTest';
import Executions from './pages/developpeur/Executions';
import ResultatsTests from './pages/developpeur/ResultatsTests';
import AnalyseQualite from './pages/developpeur/AnalyseQualite';
import Profil from './pages/developpeur/Profil';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import AdminAccueil from './pages/admin/AdminAccueil';
import AdminUtilisateurs from './pages/admin/AdminUtilisateurs';
import AdminProjets from './pages/admin/AdminProjets';
import AdminRapports from './pages/admin/AdminRapports';
import AdminLayout from './components/admin/AdminLayout';
import useAuth from './hooks/useAuth';
import { AlertDialogProvider } from './components/AlertDialog';

function RouteProtegee({ children, role }) {
  const { utilisateur } = useAuth();

  if (!utilisateur) {
    return <Navigate to="/login" replace />;
  }

  if (role && utilisateur.role !== role) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function ApplicationDeveloppeur() {
  return (
    <AlertDialogProvider>
      <div className="app-shell">
        <Sidebar />
        <div className="app-content">
          <Header />
          <main className="app-main">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/projets" element={<Projets />} />
              <Route path="/configurations" element={<ConfigurationTest />} />
              <Route path="/executions" element={<Executions />} />
              <Route path="/resultats" element={<ResultatsTests />} />
              <Route path="/qualite" element={<AnalyseQualite />} />
              <Route path="/profil" element={<Profil />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </div>
    </AlertDialogProvider>
  );
}

function App() {
  const { utilisateur } = useAuth();

  return (
    <Routes>
      <Route
        path="/login"
        element={
          utilisateur ? (
            <Navigate
              to={utilisateur.role === 'ADMIN' ? '/admin' : '/'}
              replace
            />
          ) : (
            <Login />
          )
        }
      />
      <Route
        path="/register"
        element={
          utilisateur ? (
            <Navigate
              to={utilisateur.role === 'ADMIN' ? '/admin' : '/'}
              replace
            />
          ) : (
            <Register />
          )
        }
      />
      <Route
        path="/admin"
        element={
          <RouteProtegee role="ADMIN">
            <AlertDialogProvider>
              <AdminLayout />
            </AlertDialogProvider>
          </RouteProtegee>
        }
      >
        <Route index element={<AdminAccueil />} />
        <Route path="utilisateurs" element={<AdminUtilisateurs />} />
        <Route path="projets" element={<AdminProjets />} />
        <Route path="rapports" element={<AdminRapports />} />
      </Route>
      <Route
        path="/*"
        element={
          <RouteProtegee>
            <ApplicationDeveloppeur />
          </RouteProtegee>
        }
      />
    </Routes>
  );
}

export default App;
