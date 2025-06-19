# Bundletool

A very simplistic wrapper around [bundletool](https://developer.android.com/studio/command-line/bundletool).

### Requirements

You must have java JDK 8+ on your machine
NodeJS 12.18.0 (lower might versions work but unsupported)

### Installation

`npm install bundletool`
or
`yarn add bundletool`

### Usage

```
import bundletool from "bundletool" // or the require equivalent

await bundletool([command], [args]); // async / await

bundletool([command], [args])
  .then(response => /* do something */)
  .catch(err => /* handle err */ );
```

### API

`async bundletool(command, [args])`

| Parameter |                   Description                   | Required |   Type   |
| :-------: | :---------------------------------------------: | :------: | :------: |
|  command  |       Any command available on bundletool       |   true   |  string  |
|   args    | all arguments required for the previous command |  false   | string[] |

Check the [docs](https://developer.android.com/studio/command-line/bundletool) for more information on the commands and args available

### Contribution

Always welcome. Check the [guidelines](https://github.com/Ribeiro-Tiago/bundletool/master/CONTRIBUTION) and the [TODO](https://github.com/Ribeiro-Tiago/bundletool/master/TODO.md)

### Legal notice

By using this module you agree to this module's license and google's terms and conditions, which you can find in NOTICE.txt under the platform tools installation

# 
<a href="https://www.buymeacoffee.com/ribeirotiago" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-violet.png" alt="Buy Me A Coffee" style="height: 51px !important;width: 217px !important;" ></a>
