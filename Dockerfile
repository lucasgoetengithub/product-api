from gradle:7.0.0-jdk11
copy --chown=gradle:gradle . .
workdir .
run gradle build -x test --no-daemon
expose 8081
cmd ["gradle", "bootRun"]