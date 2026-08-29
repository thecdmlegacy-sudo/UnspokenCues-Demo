(()=>{
  if(!location.pathname.endsWith('/admin.html'))return;
  const API=(window.UC_BACKEND_URL||localStorage.getItem('uc-backend-url')||(location.hostname.endsWith('vercel.app')?location.origin:'')).replace(/\/$/,'');
  const token=localStorage.getItem('uc-cloud-token')||'';
  if(!API||!token)return;
  async function upload(field,kind){const el=document.getElementById(field);if(!el||!el.value.startsWith('data:image/'))return;msg('Uploading '+kind+'…',1);const r=await fetch(API+'/api/media',{method:'POST',headers:{'Content-Type':'application/json',Authorization:'Bearer '+token},body:JSON.stringify({kind,data:el.value})});const j=await r.json();if(!r.ok)throw new Error(j.error||'Image upload failed');el.value=j.url;renderPreview(field,field==='profileImage'?'profilePreview':field==='cardBackImage'?'cardBackPreview':'cardPreview')}
  const oldSave=save;
  save=async function(){try{await upload('profileImage','profile-photo');await upload('cardImage','card-front');await upload('cardBackImage','card-back');return await oldSave()}catch(e){msg(e.message,0)}};
})();
