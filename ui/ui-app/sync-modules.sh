#!/bin/bash

DRY_RUN=1
VERBOSE=1
MODULES=("@angular/animations" "@angular/cdk" "@angular/common" "@angular/compiler" "@angular/core" "@angular/forms" "@angular/http" "@angular/material" "@angular/platform-browser" "@angular/platform-browser-dynamic" "@angular/router" "@angular/upgrade" "@covalent/core" "@uirouter/angular" "@uirouter/angular-hybrid" "@uirouter/angularjs" "@uirouter/core" "@uirouter/rx" "rxjs" "systemjs" "tslib" "zone.js")

for module in ${MODULES[@]}; do
    echo "Synching ${module}..."

    # Determine source and destination
    src="node_modules/${module}/"
    dst="src/main/resources/static/node_modules/${module}/"

    if [[ "${module}" == "rxjs" ]]; then
        rm -rf "${dst}"{_esm*, bundles, src}
    elif [[ -e "$src/bundles" ]]; then
        find "${dst}" -type f -a -not -path '*bundles*' | xargs rm
        src="${src}bundles/"
        dst="${dst}bundles/"
    elif [[ -e "$src/_bundles" ]]; then
        find "${dst}" -type f -a -not -path '*_bundles*' | xargs rm
        src="${src}_bundles/"
        dst="${dst}_bundles/"
    elif [[ -e "$src/dist" ]]; then
        find "${dst}" -type f -a -not -path '*dist*' | xargs rm
        src="${src}dist/"
        dst="${dst}dist/"
    elif [[ -e "$src/release" ]]; then
        find "${dst}" -type f -a -not -path '*release*' | xargs rm
        src="${src}release/"
        dst="${dst}release/"
    fi

    # Build command-line
    args="-a --delete"

    if [[ "$DRY_RUN" == 1 ]]; then
        args="${args} -n"
    fi
    if [[ "$VERBOSE" == 1 ]]; then
        args="${args} -v"
    fi

    if [[ "${module}" == "@angular/material" ]]; then
        rm -rf "${dst}material."*
        args="${args} --exclude=material.*"
    elif [[ "${module}" == "@covalent/core" ]]; then
        rm -rf "${dst}covalent-core."*
        args="${args} --exclude=covalent-core.*"
    elif [[ "${module}" == "rxjs" ]]; then
        args="${args} --exclude=_esm2015/ --exclude=_esm5  --exclude=bundles/ --exclude=src/ --exclude=*.js.map --exclude=*.ts --exclude=package.json"
    fi

    # Copy files
    mkdir -p "${dst}"
    
    if [[ "${module}" == "systemjs" ]]; then
        cp "${src}system.js" "${dst}system.js"
    elif [[ "${module}" == "tslib" ]]; then
        cp "${src}tslib.js" "${dst}tslib.js"
    elif [[ "${module}" == "zone.js" ]]; then
        cp "${src}zone.js" "${dst}zone.js"
        cp "${src}zone.min.js" "${dst}zone.min.js"
    else
        rsync $args "${src}" "${dst}"
    fi

    # Add to git
    git add -A -f "${dst}"
done
