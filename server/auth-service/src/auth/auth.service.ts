import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { CreateUserDto } from './dto/create-user.dto';
import { LoginUserDto } from './dto/login-user.dto';

@Injectable()
export class AuthService {
  // Simulação de um banco de dados de usuários
  private users = [];

  constructor(private readonly jwtService: JwtService) {}

  async register(createUserDto: CreateUserDto) {
    const salt = await bcrypt.genSalt();
    const hashedPassword = await bcrypt.hash(createUserDto.password, salt);

    const user = {
      email: createUserDto.email,
      password: hashedPassword,
    };

    // Simula o salvamento do usuário no banco de dados
    this.users.push(user);

    return { message: 'User registered successfully' };
  }

  async login(loginUserDto: LoginUserDto) {
    const user = this.users.find(u => u.email === loginUserDto.email);

    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const isPasswordMatching = await bcrypt.compare(loginUserDto.password, user.password);

    if (!isPasswordMatching) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const payload = { email: user.email, sub: 'user-id' }; // O 'sub' (subject) deve ser o ID do usuário
    return {
      access_token: this.jwtService.sign(payload),
    };
  }
}
