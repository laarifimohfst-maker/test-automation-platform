function Header() {
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', padding: '16px 20px', borderBottom: '1px solid #e5e7eb', gap: '16px', backgroundColor: '#ffffff' }}>
        <span>🔔</span>
        <div style={{ width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#c4b5fd' }}></div>
      </div>

      <div style={{ padding: '20px' }}>
        <h2 style={{ margin: 0, color: '#111827' }}>Bonjour, Youssef 👋</h2>
        <p style={{ margin: 0, color: '#6b7280' }}>Voici un aperçu de vos projets et exécutions.</p>
      </div>
    </div>
  );
}

export default Header;