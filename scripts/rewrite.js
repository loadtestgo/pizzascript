// Open the browser, but don't navigate to reddit just yet
var b = pizza.open();

// Rewrite all Reddit thumbnails with cats
b.rewriteUrl('https://*.thumbs.redditmedia.com/*.png',
             'http://pngimg.com/upload/cat_PNG118.png');

b.rewriteUrl('https://*.thumbs.redditmedia.com/*.jpg',
             'http://pngimg.com/upload/cat_PNG118.png');

// After the rewrites rules are in place, load the page for some cat action
b.open("https://www.reddit.com/r/DogPictures");
