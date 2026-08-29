const {getDb,requireUser,sendError}=require('./_backend');
module.exports=async function(req,res){
  res.setHeader('Access-Control-Allow-Origin','*');res.setHeader('Access-Control-Allow-Headers','Content-Type, Authorization');res.setHeader('Access-Control-Allow-Methods','GET,POST,DELETE,OPTIONS');res.setHeader('Cache-Control','no-store');
  if(req.method==='OPTIONS')return res.status(204).end();
  try{
    const u=requireUser(req),db=getDb(),col=db.collection('users').doc(u.sub).collection('binder');
    if(req.method==='GET'){
      const snap=await col.orderBy('updatedAt','desc').limit(200).get();return res.status(200).json({cards:snap.docs.map(d=>d.data())});
    }
    if(req.method==='POST'){
      const c=req.body?.card||req.body||{};const id=String(c.id||c.cardNumber||'').trim();if(!id)return res.status(400).json({error:'Card id is required.'});
      const item={id,name:c.name||'',cardNumber:c.cardNumber||'',cardTitle:c.cardTitle||'',cardImage:c.cardImage||'',cardBackImage:c.cardBackImage||'',profileUrl:c.profileUrl||'',type:c.type||'people',updatedAt:Date.now()};
      await col.doc(Buffer.from(id).toString('base64url')).set(item,{merge:true});return res.status(200).json({card:item});
    }
    if(req.method==='DELETE'){
      const id=String(req.query?.id||'').trim();if(!id)return res.status(400).json({error:'Card id is required.'});await col.doc(Buffer.from(id).toString('base64url')).delete();return res.status(200).json({ok:true});
    }
    return res.status(405).json({error:'Method not allowed'});
  }catch(e){sendError(res,e)}
};
