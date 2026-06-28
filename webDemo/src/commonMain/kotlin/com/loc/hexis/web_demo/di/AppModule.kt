package com.loc.hexis.web_demo.di

import com.loc.hexis.shared.ui.di.UIModules
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [UIModules::class]) @ComponentScan("com.loc.hexis.web_demo") class AppModule