const {getDb,requireUser,publicProfile,sendError}=require('./_backend');
module.exports=async function(req,res){
  res.setHeader('Access-Control-Allow-Origin','*');res.setHeader('Access-Control-Allow-Headers','Content-Type, Authorization');res.setHeader('Access-Control-Allow-Methods','GET,PUT,OPTIONS');res.setHeader('Cache-Control','no-store');
  if(req.method==='OPTIONS')return res.status(204).end();
  try{
    const db=getDb();
    if(req.method==='GET'){
      const requested=String(req.query?.id||'').trim();
      if(requested){const s=await db.collection('profiles').doc(requested).get();if(!s.exists)return res.status(404).json({error:'Profile not found'});return res.status(200).json({profile:publicProfile(s.data())});}
      const u=requireUser(req),s=await db.collection('profiles').doc(u.sub).get();return res.status(200).json({profile:s.exists?s.data():null});
    }
    if(req.method==='PUT'){
      const u=requireUser(req),incoming=req.body?.profile||req.body||{},profile={...incoming,id:u.sub,updatedAt:Date.now()};
      delete profile.email;delete profile.password; await db.collection('profiles').doc(u.sub).set(profile,{merge:true});
      return res.status(200).json({profile});
    }
    return res.status(405).json({error:'Method not allowed'});
  }catch(e){sendError(res,e)}
};
