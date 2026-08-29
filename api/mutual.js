const {getDb,requireUser,sendError}=require('./_backend');
module.exports=async function(req,res){
  res.setHeader('Access-Control-Allow-Origin','*');res.setHeader('Access-Control-Allow-Headers','Content-Type, Authorization');res.setHeader('Access-Control-Allow-Methods','POST,OPTIONS');res.setHeader('Cache-Control','no-store');
  if(req.method==='OPTIONS')return res.status(204).end();if(req.method!=='POST')return res.status(405).json({error:'Method not allowed'});
  try{
    const u=requireUser(req),db=getDb(),targetId=String(req.body?.targetId||'').trim(),answer=String(req.body?.answer||'').toLowerCase();
    if(!targetId||targetId===u.sub)return res.status(400).json({error:'Choose another profile.'});if(!['interested','maybe','no'].includes(answer))return res.status(400).json({error:'Invalid response.'});
    const ids=[u.sub,targetId].sort(),pairId=Buffer.from(ids.join('|')).toString('base64url'),ref=db.collection('mutualCues').doc(pairId);
    await db.runTransaction(async tx=>{const s=await tx.get(ref),d=s.exists?s.data():{users:ids,answers:{}};d.answers={...(d.answers||{}),[u.sub]:answer};d.updatedAt=Date.now();tx.set(ref,d,{merge:true});});
    const after=(await ref.get()).data()||{},other=after.answers?.[targetId],matched=answer==='interested'&&other==='interested';
    return res.status(200).json({matched,status:matched?'mutual':'private',message:matched?'Mutual Cue! You both want to connect.':'Your response is private unless you both choose Interested.'});
  }catch(e){sendError(res,e)}
};
