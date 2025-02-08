package org.example;

import org.example.entities.Train;
import org.example.entities.User;
import org.example.services.UserBookingService;
import org.example.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

public class App {

    public static void main(String[] args) throws IOException {
        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;

        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error initializing booking service. Please try again.");
            return;
        }

        Train trainSelectedForBooking = null; // Store selected train for booking

        while (option != 7) {
            System.out.println("\nChoose option:");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");

            // Ensure valid input
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1-7.");
                scanner.next(); // Consume invalid input
                continue;
            }

            option = scanner.nextInt();
            switch (option) {
                case 1:
                    scanner.nextLine(); // Consume any leftover newline
                    System.out.println("Enter the username to sign up:");
                    String nameToSignUp = scanner.nextLine(); // Use nextLine() for full input
                    System.out.println("Enter the password to sign up:");
                    String passwordToSignUp = scanner.nextLine(); // Read password properly

                    User userToSignup = new User(
                            nameToSignUp,
                            passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    userBookingService.signUp(userToSignup);
                    System.out.println("User registered successfully!");
                    break;

                case 2:
                    scanner.nextLine(); // Consume any leftover newline
                    System.out.println("Enter the username to login:");
                    String nameToLogin = scanner.nextLine();
                    System.out.println("Enter the password to login:");
                    String passwordToLogin = scanner.nextLine();

                    Optional<User> foundUser = userBookingService.getUserByUsername(nameToLogin);

                    if (foundUser.isEmpty()) {
                        System.out.println("User not found! Please sign up first.");
                        break;
                    }

                    User existingUser = foundUser.get();

                    if (UserServiceUtil.checkPassword(passwordToLogin, existingUser.getHashedPassword())) {
                        System.out.println("Login successful!");
                        userBookingService = new UserBookingService(existingUser);
                    } else {
                        System.out.println("Incorrect password. Try again.");
                    }
                    break;


                case 3:
                    System.out.println("Fetching your bookings...");
                    userBookingService.fetchBookings();
                    break;

                case 4:
                    System.out.println("Enter your source station:");
                    String source = scanner.next();
                    System.out.println("Enter your destination station:");
                    String dest = scanner.next();

                    List<Train> trains = userBookingService.getTrains(source, dest);

                    if (trains == null || trains.isEmpty()) {
                        System.out.println("No trains available for the selected route.");
                        break;
                    }

                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + ". Train ID: " + t.getTrainId());
                        for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                            System.out.println("Station: " + entry.getKey() + " - Time: " + entry.getValue());
                        }
                        index++;
                    }

                    System.out.println("Select a train by typing its number (1, 2, 3...):");

                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid input. Please enter a valid train number.");
                        scanner.next();
                        break;
                    }

                    int trainIndex = scanner.nextInt();
                    if (trainIndex < 1 || trainIndex > trains.size()) {
                        System.out.println("Invalid selection. Choose a number between 1 and " + trains.size());
                        break;
                    }

                    trainSelectedForBooking = trains.get(trainIndex - 1); // Convert to zero-based index
                    System.out.println("You selected Train ID: " + trainSelectedForBooking.getTrainId());
                    break;

                case 5:
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please search and select a train first.");
                        break;
                    }

                    System.out.println("Available seats:");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);

                    for (List<Integer> row : seats) {
                        for (Integer seat : row) {
                            System.out.print(seat + " ");
                        }
                        System.out.println();
                    }

                    System.out.println("Enter the row number:");
                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid input. Please enter a number.");
                        scanner.next();
                        break;
                    }
                    int row = scanner.nextInt();

                    System.out.println("Enter the column number:");
                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid input. Please enter a number.");
                        scanner.next();
                        break;
                    }
                    int col = scanner.nextInt();

                    System.out.println("Booking your seat...");
                    Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);

                    if (booked) {
                        System.out.println("Seat booked successfully! Enjoy your journey.");
                    } else {
                        System.out.println("Seat booking failed. Try another seat.");
                    }
                    break;

                case 6:
                    System.out.println("Enter your booking ID to cancel:");
                    String bookingId = scanner.next();

                    boolean cancelled = userBookingService.cancelBooking(bookingId);

                    if (cancelled) {
                        System.out.println("Booking cancelled successfully.");
                    } else {
                        System.out.println("No booking found with the provided ID.");
                    }
                    break;


                case 7:
                    System.out.println("Exiting the application. Thank you for using Train Booking System.");
                    break;

                default:
                    System.out.println("Invalid option. Please choose between 1-7.");
                    break;
            }
        }
        scanner.close();
    }
}
