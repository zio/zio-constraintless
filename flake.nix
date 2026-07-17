{
  description = "Dev shell for zio-constraintless (sbt + JDK)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };

        mkJdkShell =
          jdk: label:
          pkgs.mkShell {
            packages = [
              jdk
              pkgs.sbt
              pkgs.coursier # optional: `cs` for installing metals / scala tools
              pkgs.nodejs # required for Scala.js tests (`+test`)
            ];

            shellHook = ''
              export JAVA_HOME="${jdk}"
              export PATH="$JAVA_HOME/bin:$PATH"
              echo "zio-constraintless: ${label} + sbt ready ($(java -version 2>&1 | head -1))"
            '';
          };
      in
      {
        # Default: latest Temurin for local work
        devShells.default = mkJdkShell pkgs.temurin-bin-25 "JDK 25";
        # Same as CI (Temurin 17): nix develop .#jdk17
        devShells.jdk17 = mkJdkShell pkgs.temurin-bin-17 "JDK 17";
      }
    );
}
