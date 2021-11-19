# Minecraft Forge Local Example

This example is provided to help developers of the plugin debug and test versions of the plugin built from a local
source.

## Usage Guide

1. Build the plugin. This is done by running `./gradlew clean build` in the root project directory.
2. Make sure the curseforgegradle_testtoken property has been set.
3. Change the project ID to a project valid for your token.
4. Run `./gradlew clean build publishCurseForge` to build and publish this test project.

## ⚠️Security Note⚠️

Remember to never publish your auth token in a public environment such as GitHub. Auth tokens are private ifnormation
and should be treated with the same level of security as an account password. When using with a build server like
Jenkins or GitHub actions the auth token should be injected as a build secret.