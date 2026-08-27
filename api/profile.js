const demoProfile = {
  id: 'cd-001-f2',
  name: 'C&D',
  status: 'available',
  preferences: ['MFM', 'FMF', 'Couples'],
  boundaries: ['Same Room', 'Together Only', 'Conversation First'],
  cardNumber: '001-F2',
  cardTitle: 'The Disappearing Act',
  cardTagline: 'We came, we saw, we left.',
  visibility: {
    status: true,
    preferences: true,
    boundaries: true,
    card: true
  }
};

module.exports = function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Cache-Control', 'no-store');
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  res.status(200).json({ profile: demoProfile });
};
