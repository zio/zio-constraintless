{
  description = "Dev shell matching GitHub Actions (Temurin 17 + Node 16 + sbt)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    # Node 16.x as in .github/workflows/site.yml (nodejs_16 removed from unstable)
    nixpkgs-node.url = "github:NixOS/nixpkgs/nixos-23.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      nixpkgs-node,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };

        # site.yml publish-docs: node-version: 16.x
        # Marked insecure in nixpkgs only because Node 16 is EOL.
        pkgsNode = import nixpkgs-node {
          inherit system;
          config.permittedInsecurePackages = [ "nodejs-16.20.2" ];
        };
        node = pkgsNode.nodejs_16;

        # release.yml + site.yml: distribution temurin, java-version: 17
        jdk = pkgs.temurin-bin-17;

        mkShell =
          jdkPkg: jdkLabel:
          pkgs.mkShell {
            packages = [
              jdkPkg
              pkgs.sbt
              pkgs.coursier
              node
            ];

            shellHook = ''
              export JAVA_HOME="${jdkPkg}"
              export PATH="$JAVA_HOME/bin:$PATH"
              echo "zio-constraintless (CI-matched): ${jdkLabel} + sbt + node $(node -v)"
              echo "  $(java -version 2>&1 | head -1)"
            '';
          };
      in
      {
        # Default = GitHub Actions
        devShells.default = mkShell jdk "Temurin 17";
        # Optional local-only: nix develop .#jdk25
        devShells.jdk25 = mkShell pkgs.temurin-bin-25 "Temurin 25";
      }
    );
}
