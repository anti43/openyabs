#!/bin/bash

#
#Einstellen des Programmverzeichnisses
unset PROGRAM_DIR
#wenn root installiert, dann /usr/local/bin/
#wenn user installiert, dann ~/bin/
#wenn es wo anders hin soll, dann in der folgenden Zeile anders eintragen
#PROGRAM_DIR=""
#
#######################


MSG2="Java exec found in "
MSG3="OOPS, your java version is too old "
MSG4="You need to upgrade to JRE 1.6.x or newer from http://java.sun.com"
MSG5="Java version found: "
MSG6="Java version ok.."
MSG7="OOPS, you don't seem to have a valid JRE "
MSG8="OOPS, unable to locate java exec in "
MSG9=" hierarchy"
MSG10="Java exec not found in PATH, starting search.."
MSG11="Java exec found in PATH. Verifying.."
JARNAME="yabs.jar"



SAVEIFS=$IFS
IFS=$(echo -en "\n\b")

look_for_java()
{
  JAVADIR=/usr/java
  IFS=$'\n'
  potential_java_dirs=(`ls -1 "$JAVADIR" | sort | tac`)
  IFS=
  for D in "${potential_java_dirs[@]}"; do
    if [[ -d "$JAVADIR/$D" && -x "$JAVADIR/$D/bin/java" ]]; then
      JAVA_PROGRAM_DIR="$JAVADIR/$D/bin/"
      echo $MSG2 $JAVA_PROGRAM_DIR
      if check_version ; then
        return 0
      else
        return 1
      fi
    fi
  done
  echo $MSG8 "${JAVADIR}/" $MSG9 ; echo $MSG4
  return 1
}

check_version()
{
  JAVA_HEADER=`${JAVA_PROGRAM_DIR}java -version 2>&1 | head -n 1`
  JAVA_IYABSL=`echo ${JAVA_HEADER} | cut -f1 -d' '`

    VERSION=`echo ${JAVA_HEADER} | sed "s/java version \"\(.*\)\"/\1/"`
    if echo $VERSION | grep "^1.[0-5]" ; then
      echo $MSG3 "[${JAVA_PROGRAM_DIR}java = ${VERSION}]" ; echo $MSG4
      return 1
    else
      echo $MSG5 "[${JAVA_PROGRAM_DIR}java = ${VERSION}]" ; echo $MSG6
      return 0
    fi

}


# locate and test the java executable
if [ "$JAVA_PROGRAM_DIR" = "" ]; then
  if ! command -v java &>/dev/null; then
    echo $MSG10
    if ! look_for_java ; then
      exit 1
    fi
  else
    echo $MSG11
    if ! check_version ; then
      if ! look_for_java ; then
        exit 1
      fi
    fi
  fi
fi


# get the app dir

if [ "$PROGRAM_DIR" = "" ]
   then 
     if [ $(id -u) -eq 0 ]
        then PROGRAM_DIR=/usr/local/bin
     fi
     else 
        PROGRAM_DIR=$HOME/bin
fi


#Programmpfad zusammenbauen
YABS=$PROGRAM_DIR/YaBS

mkdir -p $YABS
cp -r * $YABS


echo "Program directory: ${YABS}"



# build the classpath
for FILE in ${YABS}/*.jar; do
   CLASSPATH="${CLASSPATH:+${CLASSPATH}:}$FILE"
done

IFS=$SAVEIFS

echo "#!/bin/bash" > yabs.sh
echo "CLASSPATH=${CLASSPATH}" >> yabs.sh
echo "IFS=$SAVEIFS" >> yabs.sh
echo "${JAVA_PROGRAM_DIR}java -jar $YABS/$JARNAME $1 $2 $3 $4 $5 $6" >> yabs.sh
chmod +x yabs.sh
mv  yabs.sh $YABS
pushd . > /dev/null
cd $PROGRAM_DIR
ln -s YaBS/yabs.sh yabs
popd > /dev/null

echo "Installation beendet. Programmstart mit 'yabs'"

sync


