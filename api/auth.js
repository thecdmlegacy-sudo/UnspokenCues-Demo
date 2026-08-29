const {getDb,signUser,hashPassword,verifyPassword,cleanEmail,id,sendError}=require('./_backend');
module.exports=async function(req,res){
  res.setHeader('Access-Control-Allow-Origin','*');res.setHeader('Access-Control-Allow-Headers','Content-Type, Authorization');res.setHeader('Access-Control-Allow-Methods','POST,OPTIONS');
  if(req.method==='OPTIONS')return res.status(204).end(); if(req.method!=='POST')return res.status(405).json({error:'Method not allowed'});
  try{
    const db=getDb(), action=String(req.body?.action||'guest');
    if(action==='guest'){
      const userId='guest_'+id(); await db.collection('users').doc(userId).set({id:userId,kind:'guest',createdAt:Date.now(),updatedAt:Date.now()});
      return res.status(200).json({token:signUser(userId,'guest'),user:{id:userId,kind:'guest'}});
    }
    const email=cleanEmail(req.body?.email), password=String(req.body?.password||'');
    if(!email.includes('@')||password.length<6)return res.status(400).json({error:'Use a valid email and a password of at least 6 characters.'});
    const key=Buffer.from(email).toString('base64url'); const ref=db.collection('accounts').doc(key); const snap=await ref.get();
    if(action==='register'){
      if(snap.exists)return res.status(409).json({error:'Account already exists.'});
      const userId='u_'+id(), h=hashPassword(password); await ref.set({email,userId,...h,createdAt:Date.now(),updatedAt:Date.now()});
      await db.collection('users').doc(userId).set({id:userId,email,kind:'account',createdAt:Date.now(),updatedAt:Date.now()});
      return res.status(200).json({token:signUser(userId),user:{id:userId,email,kind:'account'}});
    }
    if(action==='login'){
      if(!snap.exists)return res.status(401).json({error:'Email or password is incorrect.'}); const a=snap.data();
      if(!verifyPassword(password,a.salt,a.hash))return res.status(401).json({error:'Email or password is incorrect.'});
      return res.status(200).json({token:signUser(a.userId),user:{id:a.userId,email:a.email,kind:'account'}});
    }
    return res.status(400).json({error:'Unknown action.'});
  }catch(e){sendError(res,e)}
};
