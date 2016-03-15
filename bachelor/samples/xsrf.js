var app = express();

app.use( function( req, res, next ){
    res.set({ "Content-Security-Policy" : "script-src 'self'" });
    next();
});
