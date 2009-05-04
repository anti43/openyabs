Summary: MP Version 5
Name: MP
Version: 5
Release: 0
License: GPL
Group: Applications/Office
BuildRoot: %{_builddir}/%{name}-root 
URL: http://code.google.com/p/mp-rechnungs-und-kundenverwaltung/
Vendor: anti43
Packager: Andreas Weber
Prefix: /usr/local
BuildArchitectures: noarch

%define my_dir /home/anti/NetBeansProjects/trunk/MPV5
%define Version 5
%define Release 0

%description
MP Crossplatform Billing and Accounting Application

%prep


%build
cd %{my_dir}/dist/
rm -rf README.TXT
cp %{my_dir}/install/rpm/run.sh run.sh
tar -cf %{_builddir}/mpv%{Version}.%{Release}.tar *

%install
pwd
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}
cd $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}
tar -xf $RPM_BUILD_ROOT/../mpv%{Version}.%{Release}.tar
# Create menu entry
mkdir -p $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/misc
cat > $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/misc/mpv5.desktop <<EOF
[Desktop Entry]
Name=MP Version 5
GenericName=MPV5
Comment=Crossplatform Accounting and Billing Application
Exec=java -jar $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/MPV%{Version}.jar
Icon=$RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/misc/mpd.png
Terminal=False
StartupNotify=false
Categories=Office;X-SuSE-Core-Office
Encoding=UTF-8
Type=Application
EOF

chmod 644 $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/misc/mpv5.desktop
cp -f $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/misc/mpv5.desktop /usr/share/applications/mpv5.desktop
cp -f %{my_dir}/install/mpd.png $RPM_BUILD_ROOT/usr/local/mp/mpv%{Version}/misc/mpd.png

%postun
rm -f /usr/share/applications/mpv5.desktop

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf %{_builddir}/mpv%{Version}.%{Release}.tar *

%files 
%defattr(755,user,user)
/usr/local/mp/mpv%{Version}/MPV%{Version}.jar
/usr/local/mp/mpv%{Version}/lib/appframework-1.0.3.jar
/usr/local/mp/mpv%{Version}/lib/mail.jar
/usr/local/mp/mpv%{Version}/lib/swing-layout-1.0.3.jar
/usr/local/mp/mpv%{Version}/lib/commons-cli-20070823.jar
/usr/local/mp/mpv%{Version}/lib/microba-0.4.4.2.jar
/usr/local/mp/mpv%{Version}/lib/swing-worker-1.1.jar
/usr/local/mp/mpv%{Version}/lib/derby.jar
/usr/local/mp/mpv%{Version}/lib/MPCalendar.jar
/usr/local/mp/mpv%{Version}/lib/tinylaf.jar
/usr/local/mp/mpv%{Version}/lib/dtdparser121.jar
/usr/local/mp/mpv%{Version}/lib/org.json.jar
/usr/local/mp/mpv%{Version}/lib/xercesImpl.jar
/usr/local/mp/mpv%{Version}/lib/jdom.jar
/usr/local/mp/mpv%{Version}/lib/resolver.jar
/usr/local/mp/mpv%{Version}/lib/xml-apis.jar
/usr/local/mp/mpv%{Version}/lib/l2fprod-common-all.jar
/usr/local/mp/mpv%{Version}/lib/serializer.jar
/usr/local/mp/mpv%{Version}/misc/mpd.png
/usr/local/mp/mpv%{Version}/misc/mpv5.desktop

%attr(755,root,root) /usr/local/mp/mpv%{Version}/run.sh


%changelog
* Sun May 03 2009 Anti43
- Created initial spec file