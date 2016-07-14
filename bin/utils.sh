function get_property
{
  PROP_FILE=$1
  PROP_KEY=$2
  PROP_VALUE=`cat $PROP_FILE | grep "$PROP_KEY" | cut -d'=' -f2`

  echo $PROP_VALUE
}

function get_version
{
  buildNumber=$(get_property "gradle.properties" "buildNumber")
  majorVersion=$(get_property "gradle.properties" "majorVersion")

  echo "$majorVersion.$buildNumber"
}

function gitcheck
{
  git fetch

  LOCAL=$(git rev-parse HEAD)
  REMOTE=$(git rev-parse @{u})
  BASE=$(git merge-base HEAD @{u})

  if [ $LOCAL = $REMOTE ]; then
    echo "Up-to-date"
  elif [ $LOCAL = $BASE ]; then
    echo "Remote changes"
    read -p "Want to pull them? (y/N) " -n 1 -r
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      git pull
    fi
  elif [ $REMOTE = $BASE ]; then
    echo "Local changes not pushed"
    read -p "Want to push them? (y/N)" -n 1 -r
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      git push
    fi
  else
    echo "Local and remote have diverged."
    read -p "Continue? (y/N)" -n 1 -r
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      exit
    fi
  fi
}

function verifyZipInit
{
  LOCAL=$PROJECT/build/distributions/$PROJECT-$VERSION.zip
  TMP_DIR=/tmp/verifyZip
  if [ -d $TMP_DIR ]; then
    rm -Rf $TMP_DIR
  fi

  echo "Checking for build errors (if you are running intellij it can delete files during the build process!)"

  mkdir $TMP_DIR
  unzip $LOCAL -d $TMP_DIR
}

function verifyZipCheck
{
  FILE="$TMP_DIR/$1"
  if [ ! -f $FILE ]; then
    echo "missing file $FILE"
    exit -1
  fi
}

function verifyZipWebShared
{
  unzip -o $TMP_DIR/$PROJECT-$VERSION/lib/web-shared-$VERSION.jar -d $TMP_DIR || exit 1
  verifyZipCheck "testresults.html"
  verifyZipCheck "css/viewresult.css"
  verifyZipCheck "html/viewresult.html"
  verifyZipCheck "js/pizza.js"
  verifyZipCheck "js/viewresult.js"
  verifyZipCheck "js/jquery-1.11.min.js"
}

function verifyZipScriptEngine
{
  unzip -o $TMP_DIR/$PROJECT-$VERSION/lib/script-engine-$VERSION.jar -d $TMP_DIR || exit 1

  verifyZipCheck "chrome/Preferences.js"
  verifyZipCheck "chrome/extension/pizza.pem"
  verifyZipCheck "chrome/extension/pizza/config.js"
  verifyZipCheck "chrome/extension/pizza/content.js"
  verifyZipCheck "chrome/extension/pizza/icon128x128.png"
  verifyZipCheck "chrome/extension/pizza/icon16x16.png"
  verifyZipCheck "chrome/extension/pizza/icon48x48.png"
  verifyZipCheck "chrome/extension/pizza/jquery-2.1.1.js"
  verifyZipCheck "chrome/extension/pizza/manifest.json"
  verifyZipCheck "chrome/extension/pizza/namespace.js"
  verifyZipCheck "chrome/extension/pizza/pizza.js"
}

function verifyZipDone
{
  rm -Rf $TMP_DIR

  echo "Looks OK!"
}
