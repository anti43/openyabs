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
Prefix: %{install_dir}
BuildArchitectures: noarch
Requires:   java >= 1.6

%define my_dir /home/anti/NetBeansProjects/MPV5
%define install_dir /usr/local
%define Version 5
%define Release 0

%description
MP Crossplatform Billing and Accounting Application

%prep


%build
cd %{my_dir}/dist/
rm -rf README.TXT
tar -cf %{_builddir}/mpv%{Version}.%{Release}.tar *

%install
pwd
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}
cd $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}
tar -xf $RPM_BUILD_ROOT/../mpv%{Version}.%{Release}.tar

# Create menu entry
mkdir -p $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}/misc
cat > $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}/misc/mpv%{Version}.desktop <<EOF
[Desktop Entry]
Name=MP Version %{Version}
GenericName=MPV%{Version}
Comment=Crossplatform Accounting and Billing Application
Exec=java -jar %{install_dir}/mp/mpv%{Version}/MPV%{Version}.jar
Icon=%{install_dir}/mp/mpv%{Version}/misc/mpd.png
Terminal=False
StartupNotify=false
Categories=Office;X-SuSE-Core-Office
Encoding=UTF-8
Type=Application
EOF

# Create run script
cat > $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}/misc/mp%{Version}.sh <<EOF
#!/bin/bash
java -jar %{install_dir}/mp/mpv%{Version}/MPV%{Version}.jar
EOF

chmod 644 $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}/misc/mpv5.desktop
cp -f $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}/misc/mpv5.desktop /usr/share/applications/mpv5.desktop
cp -f %{my_dir}/install/mpd.png $RPM_BUILD_ROOT%{install_dir}/mp/mpv%{Version}/misc/mpd.png

%post
ln -fs %{install_dir}/mp/mpv%{Version}/misc/mp%{Version}.sh  /usr/bin/mp%{Version}

%postun
rm -f /usr/share/applications/mpv%{Version}.desktop
unlink /usr/bin/mp%{Version}

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf %{_builddir}/mpv%{Version}.%{Release}.tar *

%files
%defattr(755,user,user)
%{install_dir}/mp/mpv%{Version}/MPV%{Version}.jar
%{install_dir}/mp/mpv%{Version}/lib/*.jar
%{install_dir}/mp/mpv%{Version}/misc/mpd.png
%{install_dir}/mp/mpv%{Version}/misc/mpv%{Version}.desktop

%attr(755,root,root)
%{install_dir}/mp/mpv%{Version}/misc/mp%{Version}.sh



%changelog
* Sun Jun 21 2009 Anti43
- Added Requires Java >= 1.6
- Added symlink creation of /usr/bin/mp5
* Sun May 03 2009 Anti43
- Created initial spec file
