const {getBucket,requireUser,id,sendError}=require('./_backend');
module.exports=async function(req,res){
  res.setHeader('Access-Control-Allow-Origin','*');res.setHeader('Access-Control-Allow-Headers','Content-Type, Authorization');res.setHeader('Access-Control-Allow-Methods','POST,OPTIONS');
  if(req.method==='OPTIONS')return res.status(204).end();if(req.method!=='POST')return res.status(405).json({error:'Method not allowed'});
  try{
    const u=requireUser(req),data=String(req.body?.data||''),kind=String(req.body?.kind||'image').replace(/[^a-z0-9_-]/gi,'').slice(0,24);
    const m=data.match(/^data:(image\/(?:png|jpeg|webp));base64,(.+)$/i);if(!m)return res.status(400).json({error:'PNG, JPEG, or WebP image required.'});
    const buf=Buffer.from(m[2],'base64');if(buf.length>8*1024*1024)return res.status(413).json({error:'Image must be under 8 MB.'});
    const ext=m[1].toLowerCase().includes('png')?'png':m[1].toLowerCase().includes('webp')?'webp':'jpg',bucket=getBucket(),name=`users/${u.sub}/${kind}-${id()}.${ext}`,file=bucket.file(name),token=id();
    await file.save(buf,{metadata:{contentType:m[1],metadata:{firebaseStorageDownloadTokens:token}},resumable:false});
    const url=`https://firebasestorage.googleapis.com/v0/b/${encodeURIComponent(bucket.name)}/o/${encodeURIComponent(name)}?alt=media&token=${token}`;
    return res.status(200).json({url,path:name});
  }catch(e){sendError(res,e)}
};
