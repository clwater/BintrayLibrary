# 如何用最简单的方法发布Android library到jCenter(Bintray)

> 最初的想法是做一个很简单基础的网络请求的封装作为一个自己常用的网络库(这是另一个故事了), 在这个故事里, 遇到了很多... 意想不到的问题,在此留作记录,希望可以帮助到大家.

## 不想看太多只想简单操作就完事部分

### Bintray 注册(虽然啰嗦, 但是这个不提的话很容易遇到问题的)
[Bintray注册地址: https://bintray.com/signup/oss](https://bintray.com/signup/oss) 注意这里的地址是有带有oss的,是注册的个人账户

以下是个人注册的页面

![Bintray个人账户注册](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129224052.png)


以下是组织注册的页面,可以看到需要你填写额外的内容

![Bintray组织账户注册](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129224153.png)


通过github授权登录就可以了,填写相关的信息就可以了

![填写相关信息](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129225105.png)


在上传之前, 我们要先建立一个Repository

![创建Repository](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129225334.png)

Type记得要选择Maven

![Repository选择](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129225655.png)

创建成功后页面显示如下

![创建成功](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129225856.png)

<!-- ![](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129225748.png) -->


### 本地Library Module修改

本地需要修改下面三个文件

![修改相关](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191129230325.png)

1. 项目的gradle文件 加入下面内容

![项目gradle](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130200729.png)

```java
        classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4'
        classpath 'com.github.dcendents:android-maven-gradle-plugin:2.1'
```

2. library内的gradle修改

![librarygradle](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130201253.png)

将下面的内容追加到文件内

```java
// 以下内容是将Library上传到Bintray的相关配置
apply plugin: 'com.github.dcendents.android-maven'
apply plugin: 'com.jfrog.bintray'

//以下是需要针对项目需要修改的配置内容
//发布者的组织名称
group = "com.clwater"
// 版本号，下次更新是只需要更改版本号即可
version = "0.0.1"
//上面配置后上传至bintray后的编译路径是这样的： compile 'com.clwater:bintraylibrary:0.0.1'

Properties properties = new Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())
//读取 local.properties 文件里面的 bintray.user
def bintrayUser = properties.getProperty("bintray.user")
//读取 local.properties 文件里面的 bintray.apikey
def bintrayKey = properties.getProperty("bintray.apikey")

//项目主页
def siteUrl = 'https://github.com/clwater/BintrayLibrary'
//项目的版本控制地址
def gitUrl = 'https://github.com/clwater/BintrayLibrary.git'
//发布到JCenter上的项目名字，必须填写
def libName = "testlibrary"
//文档连接
def javaDocLinks = "https://github.com/clwater"
//Bintray中Repository的名字
def bintrayRepo = "ClwaterRepository"
//应用的描述
def bintrayDesc = "Desc"
//组织的名字, 建立组织账号后上传的时候需要这个, 个人张海时候不需要修改这部分
def bintrayUserOrg = "ClwaterRepository"

//生成源文件
task sourcesJar(type: Jar) {
    from android.sourceSets.main.java.srcDirs
    classifier = 'sources'
}
//生成文档
task javadoc(type: Javadoc) {
    source = android.sourceSets.main.java.srcDirs
    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))
    //以下两个为项目中带有中文注释的支持
    options.encoding "UTF-8"
    options.charSet 'UTF-8'
    options.author true
    options.version true
    options.links javaDocLinks
    failOnError false
}

//文档打包成jar
task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    from javadoc.destinationDir
}
//拷贝javadoc文件
task copyDoc(type: Copy) {
    from "${buildDir}/docs/"
    into "docs"
}

//上传到jcenter所需要的源码文件
artifacts {
    archives javadocJar
    archives sourcesJar
}

// 配置maven库，生成POM.xml文件
install {
    repositories.mavenInstaller {
        // This generates POM.xml with proper parameters
        pom {
            project {
                packaging 'aar'
                //项目名称和描述
                name 'pom project name'
                description 'pom project escription'
                url siteUrl
                licenses {
                    license {
                        //开源协议,在bintray中创建仓库时选择的license为Apache-2.0，复制下面的就可以
                        name 'The Apache Software License, Version 2.0'
                        url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }
                developers {
                    //开发者的个人信息
                    developer {
                        id 'developer id '
                        name 'developer name '
                        email 'developer email'
                    }
                }
                scm {
                    connection gitUrl
                    developerConnection gitUrl
                    url siteUrl
                }
            }
        }
    }
}

//上传到jcenter
bintray {
    user = bintrayUser
    key = bintrayKey
    configurations = ['archives']
    pkg {
//        userOrg = bintrayUserOrg
        repo = bintrayRepo
        name = libName
        desc =  bintrayDesc
        websiteUrl = siteUrl
        vcsUrl = gitUrl
        licenses = ["Apache-2.0"]
        publish = true
    }
}

```

3. local.properties修改

在这里需要配置下Bintray内的信息

![local.properties](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130201543.png)

可以通过如下步骤来获取你的api

![bintary信息](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130204313.png)


### 上传到Bintray

进入项目分别执行以下执行

```bash
./gradlew install
./gradlew bintrayUpload
```

成功执行后可以在你的Bintray中看到你刚刚上传的Library

![上传到Bintray成功](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130201802.png)



### 提交到jCenter

可以在页面详情提交到jCenter,不过需要审核才能在默认的情况下引入你的库

![提交到jCenter](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130201926.png)

那么如果不提交到jCenter中就无法使用了么, 当然不会的,在详情页面还有你自己的仓库maven地址和依赖引入地址,你可以把这些内容配置到你的项目中,就可以使用了

![本地配置](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130202025.png)

配置完成后就可以直接引用了

![本地引用](https://update-image.oss-cn-shanghai.aliyuncs.com/upImage/20191130202106.png)


## 一顿操作猛如虎, 问题总比办法多部分(常见问题)

1. Could not create package 'xxx/xxx/xxx': HTTP/1.1 404 Not Found [message:Repo 'xxx' was not found]

    解决: 需要先在Bintray建立名为xxx的Repository

2. Could not create version ‘0.1’: HTTP/1.1 401 Unauthorized [message:This resource requires authentication]
    解决: 一是local.properties内的名字和api有问题
         二是你注册了组织账号,需要额外配置userOrg(值为你注册时填写的组织名称,或者到Bintray中查看也可以找到)
3. 没有dd to JCenter按钮
    注册了组织账号, 需要个人账号来


## 相关代码

相关代码可以在[我的GitHub](https://github.com/clwater/BintrayLibrary)找到.
