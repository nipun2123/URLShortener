# Shortfy - AWS CDK Infrastructure

This AWS CDK project defines the infrastructure for Shortfy, a serverless URL shortener.

## 🚀 Deploying the Stack

Ensure you have AWS CDK installed and run:

```bash
cdk bootstrap
cdk synth
cdk deploy
```

## 🛠️ Customizing the Stack

* Modify the `src/main/java/com/myorg/ShortifyAppStack.java` file to adjust configurations.

## 📝 Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation
 * `cdk destroy`     destroy the resources created by CDK app

📌 Notes

* It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven-compatible Java IDE to build and run tests.

* Ensure IAM permissions are correctly set before deploying.

* Configure your AWS CLI with the necessary credentials.

Let me know if you’d like any refinements! 🚀
