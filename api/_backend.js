const crypto = require('crypto');
const admin = require('firebase-admin');

function serviceAccount() {
  if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
    const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
    const obj = JSON.parse(raw);
    if (obj.private_key) obj.private_key = obj.private_key.replace(/\\n/g, '\n');
    return obj;
  }
  if (process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL && process.env.FIREBASE_PRIVATE_KEY) {
    return {
      project_id: process.env.FIREBASE_PROJECT_ID,
      client_email: process.env.FIREBASE_CLIENT_EMAIL,
      private_key: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
    };
  }
  return null;
}

function getDb() {
  if (!admin.apps.length) {
    const sa = serviceAccount();
    if (!sa) throw new Error('Backend is not configured. Add Firebase server credentials.');
    admin.initializeApp({ credential: admin.credential.cert(sa), projectId: sa.project_id });
  }
  return admin.firestore();
}

function secret() {
  const s = process.env.UC_SESSION_SECRET;
  if (!s || s.length < 24) throw new Error('Backend session secret is not configured.');
  return s;
}

function b64(v) { return Buffer.from(v).toString('base64url'); }
function signUser(userId, kind='account') {
  const payload = JSON.stringify({ sub:userId, kind, exp:Date.now()+1000*60*60*24*30 });
  const body = b64(payload);
  const sig = crypto.createHmac('sha256', secret()).update(body).digest('base64url');
  return `${body}.${sig}`;
}
function verifyToken(token) {
  if (!token || !token.includes('.')) return null;
  const [body,sig] = token.split('.');
  const expected = crypto.createHmac('sha256', secret()).update(body).digest('base64url');
  if (sig.length !== expected.length || !crypto.timingSafeEqual(Buffer.from(sig),Buffer.from(expected))) return null;
  const p = JSON.parse(Buffer.from(body,'base64url').toString('utf8'));
  if (!p.sub || !p.exp || p.exp < Date.now()) return null;
  return p;
}
function requireUser(req) {
  const h = req.headers.authorization || '';
  const token = h.startsWith('Bearer ') ? h.slice(7) : '';
  const user = verifyToken(token);
  if (!user) { const e = new Error('Unauthorized'); e.status=401; throw e; }
  return user;
}
function hashPassword(password, salt=crypto.randomBytes(16).toString('hex')) {
  const hash = crypto.scryptSync(String(password), salt, 64).toString('hex');
  return { salt, hash };
}
function verifyPassword(password, salt, hash) {
  const test = crypto.scryptSync(String(password), salt, 64);
  const saved = Buffer.from(hash,'hex');
  return test.length===saved.length && crypto.timingSafeEqual(test,saved);
}
function cleanEmail(v){ return String(v||'').trim().toLowerCase(); }
function id(){ return crypto.randomUUID(); }
function venueKey(v){ return String(v||'').trim().toLowerCase().replace(/\s+/g,' ').slice(0,120); }
function publicProfile(p={}) {
  return {
    id:p.id||'', name:p.name||'', bio:p.bio||'', status:p.status||'available',
    preferences:Array.isArray(p.preferences)?p.preferences:[], boundaries:Array.isArray(p.boundaries)?p.boundaries:[],
    cueIn:p.cueIn||{}, cardNumber:p.cardNumber||'', cardTitle:p.cardTitle||'', cardTagline:p.cardTagline||'',
    profileImage:p.profileImage||'', cardImage:p.cardImage||'', cardBackImage:p.cardBackImage||'', qrTarget:p.qrTarget||''
  };
}
function sendError(res,e){ res.status(e.status||500).json({error:e.message||'Server error'}); }
module.exports={getDb,signUser,requireUser,hashPassword,verifyPassword,cleanEmail,id,venueKey,publicProfile,sendError};
