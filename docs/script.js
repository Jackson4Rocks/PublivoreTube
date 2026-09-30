document.querySelectorAll('a[href^="#"]').forEach(a=>a.addEventListener('click',e=>{const el=document.querySelector(a.getAttribute('href'));if(el){e.preventDefault();el.scrollIntoView({behavior:'smooth',block:'start'})}}));

document.querySelectorAll('.screen-card img').forEach(img=>img.addEventListener('click',()=>{
  const overlay=document.createElement('div');
  overlay.className='lightbox';
  overlay.innerHTML='<button aria-label="Close">×</button><img alt="">';
  overlay.querySelector('img').src=img.src;
  overlay.querySelector('img').alt=img.alt;
  overlay.addEventListener('click',e=>{if(e.target===overlay||e.target.tagName==='BUTTON') overlay.remove()});
  document.body.appendChild(overlay);
}));