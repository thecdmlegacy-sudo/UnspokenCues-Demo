module.exports = function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.status(200).json({
    ok: true,
    service: 'Unspoken Cues Demo API',
    version: '0.1.0-demo',
    capabilities: ['profiles', 'compatibility', 'swap']
  });
};
