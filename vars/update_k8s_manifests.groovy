#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags
 */
def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Adarsh097'
    def gitUserEmail = config.gitUserEmail ?: 'adarshgupta0601@gmail.com'

    echo "Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {

        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        sh """
            # Update application deployment
            sed -i "s|image: adarsh5559/easyshop-app:.*|image: adarsh5559/easyshop-app:${imageTag}|g" ${manifestsPath}/08-easyshop-deployment.yaml

            # Update migration job if exists
            if [ -f "${manifestsPath}/12-migration-job.yaml" ]; then
                sed -i "s|image: adarsh5559/easyshop-migration:.*|image: adarsh5559/easyshop-migration:${imageTag}|g" ${manifestsPath}/12-migration-job.yaml
            fi

            # Update ingress domain
            if [ -f "${manifestsPath}/10-ingress.yaml" ]; then
                sed -i "s|host: .*|host: easyshop.adtechs.xyz|g" ${manifestsPath}/10-ingress.yaml
            fi

            if git diff --quiet; then
                echo "No changes to commit"
            else
                git add ${manifestsPath}/*.yaml
                git commit -m "Update image tag to ${imageTag} [ci skip]"

                git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/Adarsh097/ecommerce-devops-project.git HEAD

            fi
        """
    }
}
