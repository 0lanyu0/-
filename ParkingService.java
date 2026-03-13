package com.parkingmanagement.service;

import com.parkingmanagement.dao.ParkingSpaceDAO;
import com.parkingmanagement.dao.ReservationDAO;
import com.parkingmanagement.dao.VehicleDAO;
import com.parkingmanagement.model.ParkingSpace;
import com.parkingmanagement.model.Reservation;
import com.parkingmanagement.model.Vehicle;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ParkingService {
    private ReservationDAO reservationDAO;
    private VehicleDAO vehicleDAO;
    private ParkingSpaceDAO parkingSpaceDAO;
    public ParkingService() {
        reservationDAO = new ReservationDAO();
        vehicleDAO = new VehicleDAO();
        parkingSpaceDAO = new ParkingSpaceDAO();
    }
    // 检查车辆是否存在
    public boolean checkVehicleExists(String plateNumber) {
        return vehicleDAO.getVehicleByPlateNumber(plateNumber) != null;
    }
    // 添加车辆信息
    public void addVehicle(String plateNumber, String ownerName, String gender, String contact, String color) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(plateNumber);
        vehicle.setOwnerName(ownerName);
        vehicle.setGender(gender);
        vehicle.setContact(contact);
        vehicle.setColor(color); // 设置车辆颜色
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        vehicleDAO.addVehicle(vehicle);
    }
    // 添加预订
    public void addReservation(Reservation reservation) {
        Vehicle vehicle = vehicleDAO.getVehicleByPlateNumber(reservation.getPlateNumber());
        if (vehicle == null) {
            throw new IllegalStateException("车辆信息不存在，无法添加预约");
        }
        reservation.setVehicleId(vehicle.getVehicleId());
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        reservation.setStatus(Reservation.STATUS_RESERVED);
        reservationDAO.addReservation(reservation);
    }
    // 取消预订
    public void cancelReservation(int reservationId) {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation != null && Reservation.STATUS_RESERVED.equals(reservation.getStatus())) {
            reservation.setStatus(Reservation.STATUS_CANCELLED);
            reservation.setUpdateTime(LocalDateTime.now());
            reservationDAO.updateReservation(reservation);
        } else {
            throw new IllegalArgumentException("预订状态不可取消");
        }
    }
    // 更新预订
    public void updateReservation(Reservation reservation) {
        reservation.setUpdateTime(LocalDateTime.now());
        reservationDAO.updateReservation(reservation);
    }
    // 确认预订（用户入场）
    public void confirmReservation(int reservationId) {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation != null && Reservation.STATUS_RESERVED.equals(reservation.getStatus())) {
            reservation.setStatus(Reservation.STATUS_USED);
            reservation.setUpdateTime(LocalDateTime.now());
            reservationDAO.updateReservation(reservation);
        } else {
            throw new IllegalArgumentException("预订状态不可确认");
        }
    }
    // 车辆入场
    public void checkInVehicle(int vehicleId, int spaceId) {
        ParkingSpace parkingSpace = parkingSpaceDAO.getParkingSpaceById(spaceId);
        if (parkingSpace == null) {
            throw new IllegalArgumentException("车位不存在");
        }
        if (!parkingSpace.isAvailable() || parkingSpace.isOccupied()) {
            throw new IllegalArgumentException("车位不可用或已被占用");
        }
        Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("车辆不存在");
        }
        parkingSpace.setOccupied(true);
        parkingSpace.setVehicleId(vehicleId);
        parkingSpace.setUpdateTime(LocalDateTime.now());
        parkingSpaceDAO.updateParkingSpace(parkingSpace);
        // 检查是否有该车辆的有效预订
        List<Reservation> reservations = reservationDAO.getReservationsByVehicleId(vehicleId);
        for (Reservation reservation : reservations) {
            if (Reservation.STATUS_RESERVED.equals(reservation.getStatus())) {
                reservation.setStatus(Reservation.STATUS_USED);
                reservation.setUpdateTime(LocalDateTime.now());
                reservationDAO.updateReservation(reservation);
                break;
            }
        }
    }
    // 车辆出场，计算费用并更新状态
    public double checkOutVehicle(int reservationId) {
        Reservation reservation = reservationDAO.getReservationById(reservationId);
        if (reservation == null || !Reservation.STATUS_USED.equals(reservation.getStatus())) {
            throw new IllegalArgumentException("无效的停车记录");
        }
        ParkingSpace parkingSpace = parkingSpaceDAO.getParkingSpaceById(reservation.getSpaceId());
        if (parkingSpace == null || !parkingSpace.isOccupied() ||
                parkingSpace.getVehicleId() != reservation.getVehicleId()) {
            throw new IllegalArgumentException("车位状态异常");
        }
        // 计算停车时间和费用
        LocalDateTime checkInTime = reservation.getCreateTime();
        LocalDateTime checkOutTime = LocalDateTime.now();
        Duration duration = Duration.between(checkInTime, checkOutTime);
        long hours = duration.toHours();
        if (duration.toMinutes() % 60 > 0) hours++;
        double fee = hours * 5.0;
        // 更新车位和预订状态
        parkingSpace.setOccupied(false);
        parkingSpace.setVehicleId(null);
        parkingSpace.setUpdateTime(checkOutTime);
        parkingSpaceDAO.updateParkingSpace(parkingSpace);
        reservation.setStatus(Reservation.STATUS_USED);
        reservation.setUpdateTime(checkOutTime);
        reservation.setFee(fee);
        reservationDAO.updateReservation(reservation);
        return fee;
    }
}