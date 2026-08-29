const {getDb,requireUser,venueKey,publicProfile,sendError}=require('./_backend');
module.exports=async function(req,res){
  res.setHeader('Access-Control-Allow-Origin','*');res.setHeader('Access-Control-Allow-Headers','Content-Type, Authorization');res.setHeader('Access-Control-Allow-Methods','GET,POST,OPTIONS');res.setHeader('Cache-Control','no-store');
  if(req.method==='OPTIONS')return res.status(204).end();
  try{
    const u=requireUser(req),db=getDb();
    if(req.method==='POST'){
      const active=!!req.body?.active,venue=String(req.body?.venue||'').trim(),key=venueKey(venue);
      await db.collection('presence').doc(u.sub).set({userId:u.sub,active,venue,venueKey:key,status:String(req.body?.status||'available'),updatedAt:Date.now()},{merge:true});
      return res.status(200).json({ok:true,active,venue});
    }
    if(req.method==='GET'){
      const key=venueKey(req.query?.venue||''); if(!key)return res.status(200).json({people:[]});
      const snap=await db.collection('presence').where('venueKey','==',key).limit(50).get(),cutoff=Date.now()-1000*60*60*8,people=[];
      for(const d of snap.docs){const v=d.data();if(!v.active||v.userId===u.sub||v.updatedAt<cutoff)continue;const ps=await db.collection('profiles').doc(v.userId).get();if(!ps.exists)continue;people.push({...publicProfile(ps.data()),presence:{status:v.status,venue:v.venue,updatedAt:v.updatedAt}});}
      return res.status(200).json({people});
    }
    return res.status(405).json({error:'Method not allowed'});
  }catch(e){sendError(res,e)}
};
