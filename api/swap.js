module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Allow-Methods', 'POST,OPTIONS');
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });

  const from = req.body?.from || 'demo-user';
  const to = req.body?.to || 'cd-001-f2';
  const cardNumber = req.body?.cardNumber || '001-F2';

  res.status(200).json({
    ok: true,
    swapId: `demo-${Date.now()}`,
    from,
    to,
    cardNumber,
    collected: true,
    message: 'S.W.A.P. recorded for demo flow.'
  });
};
