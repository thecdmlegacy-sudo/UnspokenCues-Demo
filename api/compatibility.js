function normalize(values = []) {
  return new Set(values.map(v => String(v).trim().toLowerCase()));
}

function intersection(a, b) {
  return [...a].filter(x => b.has(x));
}

module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Allow-Methods', 'POST,OPTIONS');
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });

  const me = req.body?.me || {};
  const them = req.body?.them || {};

  const myPrefs = normalize(me.preferences);
  const theirPrefs = normalize(them.preferences);
  const sharedPreferences = intersection(myPrefs, theirPrefs);

  const myHard = normalize(me.hardBoundaries || me.boundaries);
  const theirHard = normalize(them.hardBoundaries || them.boundaries);
  const explicitConflicts = (req.body?.conflicts || []).map(String);

  const bothOpen = ['available', 'maybe'].includes(String(me.status || '').toLowerCase()) &&
                   ['available', 'maybe'].includes(String(them.status || '').toLowerCase());

  const boundaryConflict = explicitConflicts.length > 0;
  let score = 45;
  score += Math.min(sharedPreferences.length * 15, 35);
  if (bothOpen) score += 12;
  if (!boundaryConflict) score += 8;
  if (boundaryConflict) score = Math.min(score, 39);
  score = Math.max(0, Math.min(100, score));

  let level = 'Not Aligned';
  if (!boundaryConflict && score >= 80) level = 'Strong Match';
  else if (!boundaryConflict && score >= 55) level = 'Possible Match';

  res.status(200).json({
    score,
    level,
    mutual: !boundaryConflict && sharedPreferences.length > 0,
    sharedPreferences,
    boundariesCompatible: !boundaryConflict,
    bothOpen,
    privacy: 'Only derived compatibility is returned; private matching inputs do not need to be exposed.'
  });
};
