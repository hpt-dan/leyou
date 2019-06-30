const chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
const maxPos = chars.length;
function randomString(len) {
  len = len || 32;

  let pwd = '';
  for (let i = 0; i < len; i++) {
    pwd += chars.charAt(Math.floor(Math.random() * maxPos));
  }
  return pwd;
}

function getFileName(filename, dir) {
  const ext = filename.substring(filename.lastIndexOf("."));
  return dir + randomString(20) + ext;
}
export default {
  getFileName
}
