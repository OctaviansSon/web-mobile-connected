package com.example.taskboard

data class CheatItem(
    val topic: Category,
    val title: String,
    val command: String,
    val description: String
)

val cheatSheet: List<CheatItem> = listOf(
    CheatItem(Category.DOCKER, "Containers and status", "docker ps -a", "List all containers, including stopped ones"),
    CheatItem(Category.DOCKER, "Follow logs", "docker logs -f --tail 100 <container>", "Stream the last 100 log lines"),
    CheatItem(Category.DOCKER, "Shell inside", "docker exec -it <container> sh", "Open an interactive shell in a running container"),
    CheatItem(Category.DOCKER, "Rebuild stack", "docker compose up -d --build", "Rebuild images and restart the compose stack"),
    CheatItem(Category.DOCKER, "Free disk space", "docker system prune -af", "Remove unused images, containers and networks"),

    CheatItem(Category.KUBERNETES, "All pods", "kubectl get pods -A -o wide", "Pods in every namespace with node info"),
    CheatItem(Category.KUBERNETES, "Why is it failing?", "kubectl describe pod <pod>", "Events show CrashLoopBackOff, image or probe errors"),
    CheatItem(Category.KUBERNETES, "Pod logs", "kubectl logs -f <pod> --previous", "Logs of the previous crashed container"),
    CheatItem(Category.KUBERNETES, "Restart deployment", "kubectl rollout restart deploy/<name>", "Rolling restart without downtime"),
    CheatItem(Category.KUBERNETES, "Switch context", "kubectl config use-context <ctx>", "Change the active cluster"),

    CheatItem(Category.LINUX, "Service logs", "journalctl -u <service> -f", "Follow systemd unit logs"),
    CheatItem(Category.LINUX, "Listening ports", "ss -tulpn", "TCP/UDP sockets with owning processes"),
    CheatItem(Category.LINUX, "Disk usage", "df -h && du -sh * | sort -h", "Free space and biggest folders"),
    CheatItem(Category.LINUX, "Service status", "systemctl status <service>", "State, recent logs and unit file location"),

    CheatItem(Category.NETWORKING, "HTTP headers", "curl -I https://example.com", "Check status code and headers"),
    CheatItem(Category.NETWORKING, "DNS lookup", "dig +short example.com", "Resolve a name quickly"),
    CheatItem(Category.NETWORKING, "Firewall rules", "iptables -L -n -v --line-numbers", "List rules with counters"),

    CheatItem(Category.CICD, "Pretty git history", "git log --oneline --graph --decorate", "Branch graph in one screen"),
    CheatItem(Category.CICD, "Tidy last commits", "git rebase -i HEAD~3", "Squash or reword before opening a merge request"),
    CheatItem(Category.CICD, "Save work in progress", "git stash push -m \"wip\"", "Stash changes with a message"),

    CheatItem(Category.TERRAFORM, "Init", "terraform init", "Download providers and configure the backend"),
    CheatItem(Category.TERRAFORM, "Plan to file", "terraform plan -out=tfplan", "Save the plan so apply uses exactly this"),
    CheatItem(Category.TERRAFORM, "Apply plan", "terraform apply tfplan", "Apply the saved plan"),
    CheatItem(Category.TERRAFORM, "Format code", "terraform fmt -recursive", "Canonical formatting for all files"),

    CheatItem(Category.AWS, "Who am I?", "aws sts get-caller-identity", "Verify the active credentials and account"),
    CheatItem(Category.AWS, "List bucket", "aws s3 ls s3://<bucket> --recursive --human-readable", "Objects with sizes"),

    CheatItem(Category.MONITORING, "Validate Prometheus config", "promtool check config prometheus.yml", "Catch YAML and rule errors before reload"),
    CheatItem(Category.DEVSECOPS, "Scan an image", "trivy image <image>:<tag>", "Find known CVEs in a container image"),
    CheatItem(Category.DEVSECOPS, "Fail2Ban status", "fail2ban-client status sshd", "Currently banned IPs for SSH")
)
