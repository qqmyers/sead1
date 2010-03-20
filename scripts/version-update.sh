FROM="0.0.3"
TO="0.4.100"
FOLDERS="../*"

for f in $FOLDERS; do
  if [ ! -d "$f" ]; then
    continue
  fi

  FILE=$f/META-INF/MANIFEST.MF
  if [ -e "$FILE" ]; then
    sed -i -e "s/Bundle-Version: ${FROM}.qualifier/Bundle-Version: ${TO}.qualifier/g" "$FILE"
    sed -i -e "s/;bundle-version=\"${FROM}\"/;bundle-version=\"${TO}\"/g" "$FILE"
    if [ "`grep 'Bundle-Vendor:' \"$FILE\"`" = "" ]; then
      echo "Bundle-Vendor: NCSA" >> "$FILE"
    fi
  fi

  FILE=$f/feature.xml
  if [ -e "$FILE" ]; then
    sed -i -e "s/version=\"${FROM}.qualifier\"/version=\"${TO}.qualifier\"/g" "$FILE"
  fi

  for g in "$f"/*.product; do
    if [ -e "$g" ]; then
      sed -i -e "s/version=\"${FROM}.qualifier\"/version=\"${TO}.qualifier\"/g" "$g"
    fi
  done

done

sed -i -e "s/v${FROM}#/v${TO}#/g" ../edu.illinois.ncsa.mmdb.web/build.xml