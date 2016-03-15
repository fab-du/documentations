      
var options = 
{
 	secure : false
 };

function refresh_token(){
        var d = new Date( new Date().getTime() + 600000);
        var n = d.toUTCString().toString();
        options.expires = n;
}