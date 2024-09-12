## General use:

```shell
# run the complete playbook
ansible-playbook dyn_proxy.yml

# list all available tags
ansible-playbook dyn_proxy.yml --list-tags

# run with a specific tag
ansible-playbook dyn_proxy.yml --tags deploy

# dry-run
ansible-playbook dyn_proxy.yml --tags deploy --check
```

## Logging

- DynProxy: `tail -F /opt/dev/_log/dyn_proxy.log`

## Routing Setup

```mermaid
flowchart TD
    2ohm.de --> A
    A{Apache: 80/443}
    B[dyn_proxy: 6220 \n\n src: /opt/dev/dyn_proxy]
    A -->|/proxy| B
```
